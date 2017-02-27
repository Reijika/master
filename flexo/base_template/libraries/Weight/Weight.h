#ifndef WEIGHT_h
#define WEIGHT_h

#include "Arduino.h"

enum LoadType {LIGHT, HEAVY, OVERLOAD, ERROR};

class Weight{
  public:
    Weight();
    LoadType estimateWeight(int leftPressureUpper, int leftPressureLower, int leftPressureFinger, int rightPressureUpper, int rightPressureLower, int rightPressureFinger);
  private:
    const double WEIGHT_LOW_THRESHOLD = 10.0;
    const double WEIGHT_HIGH_THRESHOLD = 50.0;
    double estimate;
};

#endif
