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

  if (wristAccelX >= wristAccelY && wristAccelY >= wristAccelZ){
    return LOW_ELEV;
  }
  else if (wristAccelY >= wristAccelX && wristAccelX >= wristAccelZ){
    return MODERATE_ELEV;
  }
  else if (wristAccelY >= wristAccelZ && wristAccelZ >= wristAccelX){
    return HIGH_ELEV;
  }
  else {
    Serial.println("Don't flail your arms around when lifting.");
    return UNKNOWN_ELEV;
  }
}

//makes sure the last 25 acceleration values are within +- 20% of the group's average (no sudden impulses)
TwistType PostureDetector::checkImpulse(int neckAccelX, int neckAccelY, int neckAccelZ, int wristAccelX, int wristAccelY, int wristAccelZ){
  
  accel_x[accel_index] = wristAccelX;
  accel_y[accel_index] = wristAccelY;
  accel_z[accel_index] = wristAccelZ;

  accel_index = (accel_index + 1) % ACCELERATION_BUFFER_SIZE;

  if (start_index != ACCELERATION_BUFFER_SIZE){
    start_index++;
  }

  for (int i = 0; i < start_index; ++i){
    x_avg += float(accel_x[i]);
    y_avg += float(accel_y[i]);
    z_avg += float(accel_z[i]);
  }

  x_avg = x_avg/float(start_index);
  y_avg = y_avg/float(start_index);
  z_avg = z_avg/float(start_index);

  for (int i = 0; i < start_index; ++i){
    if(accel_x[i] < (x_avg*(1-ACCELERATION_THRESHOLD)) && accel_x[i] > (x_avg*(1+ACCELERATION_THRESHOLD))){
      return TWIST;  
    }
    if(accel_y[i] < (y_avg*(1-ACCELERATION_THRESHOLD)) && accel_y[i] > (y_avg*(1+ACCELERATION_THRESHOLD))){
      return TWIST;  
    }
    if(accel_z[i] < (z_avg*(1-ACCELERATION_THRESHOLD)) && accel_z[i] > (z_avg*(1+ACCELERATION_THRESHOLD))){
      return TWIST;  
    }
  }

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

  if (!upperTilted && !lowerTilted){
    return STRAIGHT;
  }
  else if (upperTilted && !lowerTilted){
    return PARTIALLY_BENT;
  }
  else if (upperTilted && lowerTilted){
    return FULLY_BENT;
  }
  else{
    Serial.println("How did you bend your lower back before your upper back?");
    return UNKNOWN_TILT;
  }
  
}