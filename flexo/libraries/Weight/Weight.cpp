#include "Arduino.h"
#include "Weight.h"

#define Sprintln(a) //(Serial.println(a))

Weight::Weight(){
  estimate = 0.0;
  avg = 0.0;
  weight_index = 0;
}

LoadType Weight::estimateWeight(int leftPressureUpper, int leftPressureLower, int leftPressureFinger, int rightPressureUpper, int rightPressureLower, int rightPressureFinger){
  
  //multivariable regression model based on experimental data for different lift cases
  estimate = (0.0965501*double(leftPressureUpper)) + (0.2870269*double(leftPressureLower)) + (0.1668634*double(leftPressureFinger)) + (0.0776616*double(rightPressureUpper)) + (0.2805082*double(rightPressureLower)) + (0.1465539*double(rightPressureFinger)) + 1.4096351;    
  
  //negative bound check (unnecessary, but in case the model changes in the future)
  estimate = (estimate < 0.0) ? 0 : estimate; 
  
  //weight buffering to avoid sudden changes due to minor sensor shift
  weights[weight_index] = estimate;
  weight_index = (weight_index + 1) % WEIGHT_BUFFER_SIZE;

  for (int i = 0; i < WEIGHT_BUFFER_SIZE; ++i){
    avg = avg + weights[i];
  }
  
  avg = avg/WEIGHT_BUFFER_SIZE;
  estimate = avg;
  avg = 0.0;

  Serial.print("Est: " + String(estimate) + "      ");

  //translate estimated weight into a classification
  if (estimate >= 0.0 && estimate < WEIGHT_LOW_THRESHOLD){
    Sprintln(F("LOAD_TYPE: LIGHT"));
    //Serial.println("LOAD TYPE: LIGHT");
    return LIGHT;
  }
  else if (estimate >= WEIGHT_LOW_THRESHOLD && estimate < WEIGHT_HIGH_THRESHOLD){
    Sprintln(F("LOAD_TYPE: HEAVY"));
    //Serial.println("LOAD TYPE: HEAVY");
    return HEAVY;
  }
  else if (estimate >= WEIGHT_HIGH_THRESHOLD){
    Sprintln(F("LOAD_TYPE: OVERLOAD"));
    //Serial.println("LOAD TYPE: OVERLOAD");
    return OVERLOAD;
  }

  return ERROR;
}

