#ifndef WEIGHT_h
#define WEIGHT_h

#include "Arduino.h"

enum LoadType {LIGHT, HEAVY, OVERLOAD, ERROR};

class Weight{
  public:
    Weight();
    LoadType estimateWeight(int leftPressureUpper, int leftPressureLower, int leftPressureFinger, int rightPressureUpper, int rightPressureLower, int rightPressureFinger);
  private:
	const int ACTIVE_25_THRESHOLD = 5; //25
    const int ACTIVE_100_THRESHOLD = 5; //25
    const double WEIGHT_LOW_THRESHOLD = 10.0;
    const double WEIGHT_HIGH_THRESHOLD = 50.0;
    bool leftHandActive;
    bool leftUpper;
    bool leftLower;
    bool leftFinger;
    bool rightHandActive;
    bool rightUpper;
    bool rightLower;
    bool rightFinger;
    bool upper;
    bool lower;
    bool finger;    
    double estimate;
};

#endif
