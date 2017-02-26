#include "Arduino.h"
#include "Weight.h"

Weight::Weight(){
  leftHandActive = false;
  leftUpper = false;
  leftLower = false;
  leftFinger = false;
  rightHandActive = false;
  rightUpper = false;
  rightLower = false;
  rightFinger = false;
  upper = false;
  lower = false;
  finger = false;
  estimate = 0.0;
}

LoadType Weight::estimateWeight(int leftPressureUpper, int leftPressureLower, int leftPressureFinger, int rightPressureUpper, int rightPressureLower, int rightPressureFinger){
  //check if the left hand is involved in a lift attempt
  leftUpper = (leftPressureUpper >= ACTIVE_25_THRESHOLD) ? true : false;
  leftLower = (leftPressureLower >= ACTIVE_25_THRESHOLD) ? true : false;
  leftFinger = (leftPressureFinger >= ACTIVE_100_THRESHOLD) ? true : false;
  leftHandActive = (leftUpper || leftLower || leftFinger) ? true : false;  

  //check if the right hand is involved in a lift attempt
  rightUpper = (rightPressureUpper >= ACTIVE_25_THRESHOLD) ? true : false;
  rightLower = (rightPressureLower >= ACTIVE_25_THRESHOLD) ? true : false;
  rightFinger = (rightPressureFinger >= ACTIVE_100_THRESHOLD) ? true : false;
  rightHandActive = (rightUpper || rightLower || rightFinger) ? true : false;

  //determine which pressure sensors are involved (for the 2 hand case where we assume the active sensors are symmetrical)
  upper = leftUpper || rightUpper;
  lower = leftLower || rightLower;
  finger = leftFinger || rightFinger;

  //Serial.println("LH: " + String(leftHandActive) + ", LU: " + String(leftUpper) + ", LL: " + String(leftLower) + ", LF: " + String(leftFinger) + ", RH: " + String(rightHandActive) + ", RU: " + String(rightUpper) + ", RL: " + String(rightLower) + ", RF: " + String(rightFinger));

  //specific cases
  //1-hand handle - upper, finger
  //2-hand box, lifting by edges - finger
  //2-hand box, lifting by palms below - lower, finger

  //estimate weight from regression model
  if ( (leftHandActive && rightHandActive) && (!upper && !lower && finger) ){ //2-hand edge lift
    estimate = (0.045337*double(leftPressureFinger)) + (1.17228*double(rightPressureFinger)) - 2.94041;
  }
  else if ( (leftHandActive && rightHandActive) && (!upper && lower && finger) ){ //2-hand palm lift
    estimate = (0.107801*double(leftPressureLower)) + (-0.73475*double(leftPressureFinger)) + (0.123641*double(rightPressureLower)) + (5.874704*double(rightPressureFinger)) - 48.3381;
  }
  else if ( (leftHandActive && !rightHandActive) && (leftUpper && !leftLower && leftFinger) ) { // 1 hand left handle lift
    estimate = (-0.02849*double(leftPressureUpper)) + (0.980591*double(leftPressureFinger)) - 2.36751;
  }  
  else if ( (!leftHandActive && rightHandActive) && (rightUpper && !rightLower && rightFinger) ){ // 1 hand right handle lift
    estimate = (0.057143*double(rightPressureUpper)) + (0.009455*double(rightPressureFinger)) + 1.961952;    
  }    
  else if ( !leftHandActive && !rightHandActive ){ //if both hands are inactive, no lift attempt
    estimate = 0.0;
  }
  else {  // otherwise, the user is lifting something, but it doesn't fall into the above cases
    estimate = (0.021749*double(leftPressureUpper)) + (0.009623*double(leftPressureLower)) + (0.400587*double(leftPressureFinger)) + (0.003474*double(rightPressureUpper)) + (0.09879*double(rightPressureLower)) + (0.51716*double(rightPressureFinger)) - 0.05779;
  }

  //accounts for potential negative case
  if (estimate < 0.0){
    estimate = 0.0;
  }

  //Serial.println("Weight Estimate: " + String(estimate));

  //translate estimated weight into a classification
  if (estimate >= 0.0 && estimate < WEIGHT_LOW_THRESHOLD){
    //Serial.println("LOAD TYPE: LIGHT");
    return LIGHT;
  }
  else if (estimate >= WEIGHT_LOW_THRESHOLD && estimate < WEIGHT_HIGH_THRESHOLD){
    //Serial.println("LOAD TYPE: HEAVY");
    return HEAVY;
  }
  else if (estimate >= WEIGHT_HIGH_THRESHOLD){
    //Serial.println("LOAD TYPE: OVERLOAD");
    return OVERLOAD;
  }

  return ERROR;
}

