#include "Arduino.h"
#include "Weight.h"

Weight::Weight(){
  estimate = 0.0;
}

LoadType Weight::estimateWeight(int leftPressureUpper, int leftPressureLower, int leftPressureFinger, int rightPressureUpper, int rightPressureLower, int rightPressureFinger){
  
  //multivariable regression model based on experimental data for different lift cases
  estimate = (0.0965501*double(leftPressureUpper)) + (0.2870269*double(leftPressureLower)) + (0.1668634*double(leftPressureFinger)) + (0.0776616*double(rightPressureUpper)) + (0.2805082*double(rightPressureLower)) + (0.1465539*double(rightPressureFinger)) + 1.4096351;      
  
  //negative bound check (unnecessary, but in case the model changes in the future)
  estimate = (estimate < 0.0) ? 0 : estimate; 
  
  //Serial.println("Weight Estimate: " + String(estimate));

  //translate estimated weight into a classification
  if (estimate >= 0.0 && estimate < WEIGHT_LOW_THRESHOLD){
    Serial.println("LOAD TYPE: LIGHT");
    return LIGHT;
  }
  else if (estimate >= WEIGHT_LOW_THRESHOLD && estimate < WEIGHT_HIGH_THRESHOLD){
    Serial.println("LOAD TYPE: HEAVY");
    return HEAVY;
  }
  else if (estimate >= WEIGHT_HIGH_THRESHOLD){
    Serial.println("LOAD TYPE: OVERLOAD");
    return OVERLOAD;
  }

  return ERROR;
}

