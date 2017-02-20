#ifndef WEIGHT_h
#define WEIGHT_h

#include "Arduino.h"

class Weight{
  public:
    Weight();
    int estimateWeight(int leftPressureUpper, int leftPressureLower, int leftPressureFinger, int rightPressureUpper, int rightPressureLower, int rightPressureFinger);
  private:
	const int ACTIVE_THRESHOLD = 100;
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
};

#endif
