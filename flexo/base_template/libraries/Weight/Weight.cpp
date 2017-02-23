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
  upperValue = 0;
  lowerValue = 0;
  fingerValue = 0;
  estimate = 0;
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

  //determine which pressure sensors are involved
  upper = leftUpper || rightUpper;
  lower = leftLower || rightLower;
  finger = leftFinger || rightFinger;

  // 2 hand lift 
  if (leftHandActive && rightHandActive){
    upperValue = max(leftPressureUpper, rightPressureUpper);
    lowerValue = max(leftPressureLower, rightPressureLower);
    fingerValue = max(leftPressureFinger, rightPressureFinger);    

//handle - upper, finger

//box, lifting by edges - finger, upper, lower

//box, lifting by palms below - finger, lower



    //2 handed lift sensor combinations
    if (upper && lower && finger){
      //TBA
    }
    else if (upper && lower && finger){
      //TBA      
    }
    else if (upper && lower && finger){
      //TBA      
    }
    else if (upper && lower && finger){
      
    }
  }
  // 1 hand lift
  else if ( (leftHandActive && !rightHandActive) || (!leftHandActive && rightHandActive) ){
    upperValue = leftPressureUpper;
    lowerValue = leftPressureLower;
    fingerValue = leftPressureFinger;

    //1 handed lift sensor combinations
    if (upper && lower && finger){
      //TBA      
    }
    else if (upper && lower && finger){
      //TBA      
    }
    else if (upper && lower && finger){
      //TBA      
    }
    else if (upper && lower && finger){
      //TBA      
    }    
  }    

  Serial.println("LH: " + String(leftHandActive) + ", LU: " + String(leftUpper) + ", LL: " + String(leftLower) + ", LF: " + String(leftFinger) + ", RH: " + String(rightHandActive) + ", RU: " + String(rightUpper) + ", RL: " + String(rightLower) + ", RF: " + String(rightFinger));

  //translate estimated weight into a classification
  if (estimate >= 0 && estimate < WEIGHT_LOW_THRESHOLD){
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

