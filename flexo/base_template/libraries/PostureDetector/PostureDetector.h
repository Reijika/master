#ifndef POSTUREDETECTOR_h
#define POSTUREDETECTOR_h

#include "Arduino.h"

enum ElevationType {LOW_ELEV, MODERATE_ELEV, HIGH_ELEV, UNKNOWN_ELEV};
enum TwistType {TWIST, NONE};
enum BackTiltType {STRAIGHT, PARTIALLY_BENT, FULLY_BENT, UNKNOWN_TILT};

class PostureDetector{
  public:
    PostureDetector();
    ElevationType checkArmElevation(int wristAccelX, int wristAccelY, int wristAccelZ);
    TwistType checkImpulse(int neckAccelX, int neckAccelY, int neckAccelZ, int wristAccelX, int wristAccelY, int wristAccelZ);
    BackTiltType checkBackTilt(int tiltUpperBack, int tiltLowerBack);
    void clearImpulseState();

  private:
    const int UPPER_BACK_MEDIAN_THRESHOLD = 285;
    const int LOWER_BACK_MEDIAN_THRESHOLD = 830;
    bool upperTilted;
    bool lowerTilted;    

    const float ACCELERATION_THRESHOLD = 0.2;
    const int ACCELERATION_BUFFER_SIZE = 25;
    int accel_x [25];
    int accel_y [25];
    int accel_z [25];
    float x_avg;
    float y_avg;
    float z_avg;
    int start_index;
    int accel_index;

};

#endif

