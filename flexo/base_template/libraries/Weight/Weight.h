#ifndef WEIGHT_h
#define WEIGHT_h

#include "Arduino.h"

enum LoadType {LIGHT, HEAVY, OVERLOAD, ERROR};

class Weight{
  public:
    Weight();
    LoadType estimateWeight(int leftPressureUpper, int leftPressureLower, int leftPressureFinger, int rightPressureUpper, int rightPressureLower, int rightPressureFinger);
  private:
	const int ACTIVE_THRESHOLD = 100;
    const int WEIGHT_LOW_THRESHOLD = 10;
    const int WEIGHT_HIGH_THRESHOLD = 50;
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
    int upperValue;
    int lowerValue;
    int fingerValue;
    int estimate;
};

#endif
