#include "Arduino.h"
#include "PostureDetector.h"

#define Sprintln(a) //(Serial.println(a))

PostureDetector::PostureDetector(){
  upperTilted = false;
  lowerTilted = false;
  
  start_index = 0;
  accel_index = 0;
  x_avg = 0.0f;
  y_avg = 0.0f;
  z_avg = 0.0f;

  value_x = 0;
  value_y = 0;
  value_z = 0;

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
    Sprintln(F("UP_POS_ELEV: LOW"));
    //Serial.println("UP_POS_ELEV: LOW"); 
    return LOW_ELEV;
  }
  else if (wristAccelY >= wristAccelX && wristAccelX >= wristAccelZ){
    Sprintln(F("UP_POS_ELEV: MODERATE"));
    //Serial.println("UP_POS_ELEV: MODERATE");
    return MODERATE_ELEV;
  }
  else if (wristAccelY >= wristAccelZ && wristAccelZ >= wristAccelX){
    Sprintln(F("UP_POS_ELEV: HIGH"));
    //Serial.println("UP_POS_ELEV: HIGH");
    return HIGH_ELEV;
  }

  //inequalities for right palm facing the inside position
  //low      -> x > z > y
  //moderate -> z > x > y
  //high     -> z > y > x
  else if (wristAccelX >= wristAccelZ && wristAccelZ >= wristAccelY){
    Sprintln(F("SIDE_POS_ELEV: LOW"));
    //Serial.println("SIDE_POS_ELEV: LOW");
    return LOW_ELEV;
  }
    else if (wristAccelZ >= wristAccelX && wristAccelX >= wristAccelY){
    Sprintln(F("SIDE_POS_ELEV: MODERATE"));
    //Serial.println("SIDE_POS_ELEV: MODERATE");
    return MODERATE_ELEV;
  }
  else if (wristAccelZ >= wristAccelY && wristAccelY >= wristAccelX){
    Sprintln(F("SIDE_POS_ELEV: HIGH"));
    //Serial.println("SIDE_POS_ELEV: HIGH");
    return HIGH_ELEV;
  }

  else {
    Sprintln(F("ELEVATION: ERROR"));
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
    value_x = accel_x[start_index-1];
    value_y = accel_y[start_index-1];
    value_z = accel_z[start_index-1];
    
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


  for (int i = 0; i < ACCELERATION_BUFFER_SIZE; ++i){
    if(accel_x[i] < (x_avg*(1-ACCELERATION_THRESHOLD)) || accel_x[i] > (x_avg*(1+ACCELERATION_THRESHOLD))){
      Sprintln(F("X TWIST"));
      //Serial.println("X TWIST");
      return TWIST;  
    }
    if(accel_y[i] < (y_avg*(1-ACCELERATION_THRESHOLD)) || accel_y[i] > (y_avg*(1+ACCELERATION_THRESHOLD))){
      Sprintln(F("Y TWIST"));
      //Serial.println("Y TWIST"); 
      return TWIST;             
    }
    if(accel_z[i] < (z_avg*(1-ACCELERATION_THRESHOLD)) || accel_z[i] > (z_avg*(1+ACCELERATION_THRESHOLD))){
      Sprintln(F("Z TWIST"));
      //Serial.println("Z TWIST");
      return TWIST;  
    }
  }
  
  Sprintln(F("NONE"));
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
    Sprintln(F("BACK: STRAIGHT"));
    //Serial.println("BACK: STRAIGHT");
    return STRAIGHT;
  }
  else if (upperTilted && !lowerTilted){
    Sprintln(F("BACK: PARTIALLY_BENT"));
    //Serial.println("BACK: PARTIALLY_BENT");
    return PARTIALLY_BENT;
  }
  else if (upperTilted && lowerTilted){
    Sprintln(F("BACK: FULLY_BENT"));
    //Serial.println("BACK: FULLY_BENT");    
    return FULLY_BENT;
  }
  else{
    Sprintln(F("BACK: ERROR"));
    //Serial.println("How did you bend your lower back before your upper back?");
    return UNKNOWN_TILT;
  }
  
}