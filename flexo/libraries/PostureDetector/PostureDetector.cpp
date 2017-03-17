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

  tilt_index = 0;
  tilt_start_index = 0;
  tilt_value = 0;
  tilt_max = 0;
  tilt_decision = 0;

  for (int i = 0; i < ACCELERATION_BUFFER_SIZE; ++i){
    accel_x[i] = 0;
    accel_y[i] = 0;
    accel_z[i] = 0;
  }
}

//checks the inequalities of the xyz values to determine arm elevation
ElevationType PostureDetector::checkArmElevation(int wristAccelX, int wristAccelY, int wristAccelZ){

  //inequalities for right palm up position
  //low      -> x > y > z
  //moderate -> y > x > z
  //high     -> y > z > x
  if (wristAccelX >= wristAccelY && wristAccelY >= wristAccelZ){
    Sprintln(F("UP_POS_ELEV: LOW"));
    return LOW_ELEV;
  }
  else if (wristAccelY >= wristAccelX && wristAccelX >= wristAccelZ){
    Sprintln(F("UP_POS_ELEV: MODERATE"));
    return MODERATE_ELEV;
  }
  else if (wristAccelY >= wristAccelZ && wristAccelZ >= wristAccelX){
    Sprintln(F("UP_POS_ELEV: HIGH"));
    return HIGH_ELEV;
  }

  //inequalities for right palm facing the inside position
  //low      -> x > z > y
  //moderate -> z > x > y
  //high     -> z > y > x
  else if (wristAccelX >= wristAccelZ && wristAccelZ >= wristAccelY){
    Sprintln(F("SIDE_POS_ELEV: LOW"));
    return LOW_ELEV;
  }
    else if (wristAccelZ >= wristAccelX && wristAccelX >= wristAccelY){
    Sprintln(F("SIDE_POS_ELEV: MODERATE"));
    return MODERATE_ELEV;
  }
  else if (wristAccelZ >= wristAccelY && wristAccelY >= wristAccelX){
    Sprintln(F("SIDE_POS_ELEV: HIGH"));
    return HIGH_ELEV;
  }

  else {
    Sprintln(F("ELEVATION: ERROR"));
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
      return TWIST;  
    }
    if(accel_y[i] < (y_avg*(1-ACCELERATION_THRESHOLD)) || accel_y[i] > (y_avg*(1+ACCELERATION_THRESHOLD))){
      Sprintln(F("Y TWIST"));
      return TWIST;             
    }
    if(accel_z[i] < (z_avg*(1-ACCELERATION_THRESHOLD)) || accel_z[i] > (z_avg*(1+ACCELERATION_THRESHOLD))){
      Sprintln(F("Z TWIST"));
      return TWIST;  
    }
  }
  
  Sprintln(F("NONE"));
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

  upperTilted = (tiltUpperBack > UPPER_BACK_MEDIAN_THRESHOLD) ? true : false;
  lowerTilted = (tiltLowerBack > LOWER_BACK_MEDIAN_THRESHOLD) ? true : false;

  //record the classification in the buffer
  if (!upperTilted && !lowerTilted){    
    tilt_buffer[tilt_index] = 0;
  }
  else if (upperTilted && !lowerTilted){    
    tilt_buffer[tilt_index] = 1;        
  }
  else if (upperTilted && lowerTilted){    
    tilt_buffer[tilt_index] = 2;
  }
  else{    
    tilt_buffer[tilt_index] = 3;
  }

  tilt_index = (tilt_index + 1) % TILT_BUFFER_SIZE; //update the buffer index

  //handles the initial buffer load
  if (tilt_start_index != TILT_BUFFER_SIZE){
    tilt_start_index++;
    tilt_value = tilt_buffer[tilt_start_index - 1];        
    for (int i = tilt_start_index; i < TILT_BUFFER_SIZE; ++i){
      tilt_buffer[i] = tilt_value;      
    }    
  }

  //counts the occurrence of each classification currently in the buffer
  for (int i = 0; i < TILT_BUFFER_SIZE; ++i){
    switch(tilt_buffer[i]){
      case 0:
        tilt_count[0] = tilt_count[0] + 1;
        break;
      case 1:
        tilt_count[1] = tilt_count[1] + 1;
        break;
      case 2:
        tilt_count[2] = tilt_count[2] + 1;
        break;
      default:
        tilt_count[3] = tilt_count[3] + 1;
        break;
    }
  }

  //determine which classification has the highest frequency
  for (int i = 0; i < 4; ++i){
    if (tilt_count[i] > tilt_max){
      tilt_decision = i;
    }
  }

  //reset variables for each call
  for (int i = 0; i < 4; ++i){
    tilt_count[i] = 0;
  }
  tilt_max = 0;

  //return the most frequent classification
  switch(tilt_decision){
    case 0:
      Sprintln(F("BACK: STRAIGHT"));  
      return STRAIGHT;      
    case 1:
      Sprintln(F("BACK: PARTIALLY_BENT"));  
      return PARTIALLY_BENT;
    case 2:
      Sprintln(F("BACK: FULLY_BENT"));  
      return FULLY_BENT;      
    default:
      Sprintln(F("BACK: ERROR"));  
      return UNKNOWN_TILT;
  }
  
}

//call this to clear state whenever the lift attempt finishes or is interrupted
void PostureDetector::clearBackTiltState(){
  for (int i = 0; i < TILT_BUFFER_SIZE; ++i){
    tilt_buffer[i] = 0;
  }
  
  tilt_index = 0;
  tilt_start_index = 0;  
}