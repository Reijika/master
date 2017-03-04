#include "Arduino.h"
#include "PostureDetector.h"

PostureDetector::PostureDetector(){
  upperTilted = false;
  lowerTilted = false;
  
  start_index = 0;
  accel_index = 0;
  x_avg = 0.0f;
  y_avg = 0.0f;
  z_avg = 0.0f;

  for (int i = 0; i < ACCELERATION_BUFFER_SIZE; ++i){
    accel_x[i] = 0;
    accel_y[i] = 0;
    accel_z[i] = 0;
  }
}

//checks the inequalities of the xyz values to determine arm elevation
ElevationType PostureDetector::checkArmElevation(int wristAccelX, int wristAccelY, int wristAccelZ){

  //Serial.println(String(wristAccelX) + ", " + String(wristAccelY) + ", " + String(wristAccelZ));

  //inequalities for right palm up position
  //low      -> x > y > z
  //moderate -> y > x > z
  //high     -> y > z > x
  if (wristAccelX >= wristAccelY && wristAccelY >= wristAccelZ){
    //Serial.println("UP_POS_ELEV: LOW"); 
    return LOW_ELEV;
  }
  else if (wristAccelY >= wristAccelX && wristAccelX >= wristAccelZ){
    //Serial.println("UP_POS_ELEV: MODERATE");
    return MODERATE_ELEV;
  }
  else if (wristAccelY >= wristAccelZ && wristAccelZ >= wristAccelX){
    //Serial.println("UP_POS_ELEV: HIGH");
    return HIGH_ELEV;
  }

  //inequalities for right palm facing the inside position
  //low      -> x > z > y
  //moderate -> z > x > y
  //high     -> z > y > x
  else if (wristAccelX >= wristAccelZ && wristAccelZ >= wristAccelY){
    //Serial.println("SIDE_POS_ELEV: LOW");
    return LOW_ELEV;
  }
    else if (wristAccelZ >= wristAccelX && wristAccelX >= wristAccelY){
    //Serial.println("SIDE_POS_ELEV: MODERATE");
    return MODERATE_ELEV;
  }
  else if (wristAccelZ >= wristAccelY && wristAccelY >= wristAccelX){
    //Serial.println("SIDE_POS_ELEV: HIGH");
    return HIGH_ELEV;
  }

  else {
    //Serial.println("Don't flail your arms around when lifting.");
    return UNKNOWN_ELEV;
  }
}

//makes sure the last 25 acceleration values are within +- 20% of the group's average (no sudden impulses)
TwistType PostureDetector::checkImpulse(int wristAccelX, int wristAccelY, int wristAccelZ){
  
  accel_x[accel_index] = wristAccelX;
  accel_y[accel_index] = wristAccelY;
  accel_z[accel_index] = wristAccelZ;

  accel_index = (accel_index + 1) % ACCELERATION_BUFFER_SIZE;

  if (start_index != ACCELERATION_BUFFER_SIZE){

    start_index++;

    int value_x = accel_x[start_index-1];
    int value_y = accel_y[start_index-1];
    int value_z = accel_z[start_index-1];
    
    for (int i = start_index; i < ACCELERATION_BUFFER_SIZE; ++i){
      accel_x[i] = value_x;
      accel_y[i] = value_y;
      accel_z[i] = value_z;
    }
    
  }

  for (int i = 0; i < ACCELERATION_BUFFER_SIZE; ++i){
    x_avg += float(accel_x[i]);
    y_avg += float(accel_y[i]);
    z_avg += float(accel_z[i]);
  }

  x_avg = x_avg/float(ACCELERATION_BUFFER_SIZE);
  y_avg = y_avg/float(ACCELERATION_BUFFER_SIZE);
  z_avg = z_avg/float(ACCELERATION_BUFFER_SIZE);

  // String contents = "{";
  // for (int i = 0; i < ACCELERATION_BUFFER_SIZE; ++i){
  //   contents = contents + String(accel_z[i]) + ", ";
  // }
  // contents = contents + "}";
  // Serial.print(contents + ": ");


  for (int i = 0; i < ACCELERATION_BUFFER_SIZE; ++i){
    if(accel_x[i] < (x_avg*(1-ACCELERATION_THRESHOLD)) || accel_x[i] > (x_avg*(1+ACCELERATION_THRESHOLD))){
      //Serial.println("X TWIST");
      return TWIST;  
    }
    if(accel_y[i] < (y_avg*(1-ACCELERATION_THRESHOLD)) || accel_y[i] > (y_avg*(1+ACCELERATION_THRESHOLD))){
      //Serial.println("Y TWIST"); 
      return TWIST;             
    }
    if(accel_z[i] < (z_avg*(1-ACCELERATION_THRESHOLD)) || accel_z[i] > (z_avg*(1+ACCELERATION_THRESHOLD))){
      //Serial.println("Z TWIST");
      return TWIST;  
    }
  }
  
  //Serial.println("NONE");
  return NONE;
}

//call this to clear state whenever the lift attempt finishes or is interrupted
void PostureDetector::clearImpulseState(){
  for (int i = 0; i < ACCELERATION_BUFFER_SIZE; ++i){
    accel_x[i] = 0;
    accel_y[i] = 0;
    accel_z[i] = 0;
  }
  x_avg = 0.0f;
  y_avg = 0.0f;
  z_avg = 0.0f;
  start_index = 0;
  accel_index = 0;
}

//checks which of the tilt sensors are active
BackTiltType PostureDetector::checkBackTilt(int tiltUpperBack, int tiltLowerBack){

  //Serial.println(String(tiltUpperBack) + ", " + String(tiltLowerBack));

  upperTilted = (tiltUpperBack > UPPER_BACK_MEDIAN_THRESHOLD) ? true : false;
  lowerTilted = (tiltLowerBack > LOWER_BACK_MEDIAN_THRESHOLD) ? true : false;

  if (!upperTilted && !lowerTilted){
    //Serial.println("BACK: STRAIGHT");
    return STRAIGHT;
  }
  else if (upperTilted && !lowerTilted){
    //Serial.println("BACK: PARTIALLY_BENT");
    return PARTIALLY_BENT;
  }
  else if (upperTilted && lowerTilted){
    //Serial.println("BACK: FULLY_BENT");    
    return FULLY_BENT;
  }
  else{
    //Serial.println("How did you bend your lower back before your upper back?");
    return UNKNOWN_TILT;
  }
  
}