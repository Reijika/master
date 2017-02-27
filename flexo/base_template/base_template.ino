#include <Weight.h>
#include <PostureDetector.h>

//object weight estimation
Weight scale;
LoadType load_type;

//posture detection
PostureDetector detector;
ElevationType elevation_type;
TwistType twist_type;
BackTiltType backtilt_type;


//lift timerr
unsigned long start;
unsigned long end;
bool heavy_lift = false;  //used by checkTime to check if a lift attempt has started
const int MAX_LIFT_DURATION = 60000;

//buffer constants
const int BUF_SIZE = 5;
const int POLL_DELAY = 50;

// INPUT BUFFERS
int buf_0 [BUF_SIZE];    
int buf_1 [BUF_SIZE];     
int buf_2 [BUF_SIZE];     
int buf_3 [BUF_SIZE];     
int buf_4 [BUF_SIZE];     
int buf_5 [BUF_SIZE];     
int buf_6 [BUF_SIZE];     
int buf_7 [BUF_SIZE];     
int buf_8 [BUF_SIZE];     
int buf_9 [BUF_SIZE];     
int buf_10 [BUF_SIZE];    
int buf_11 [BUF_SIZE];    
int val_12 = 0;           
int val_13 = 0;           

//filtered values - can adjust the pin to sensor mapping later
int leftPressureUpper = 0;
int leftPressureLower = 0;
int leftPressureFinger = 0;
int rightPressureUpper = 0;
int rightPressureLower = 0;
int rightPressureFinger = 0;
int neckAccelX = 0;
int neckAccelY = 0;
int neckAccelZ = 0;
int wristAccelX = 0;
int wristAccelY = 0;
int wristAccelZ = 0;
int tiltUpperBack = 0;
int tiltLowerBack = 0;

//input index and avg buffers
int buf_index[12] = {0,0,0,0,0,0,0,0,0,0,0,0};
int buf_sum[12]   = {0,0,0,0,0,0,0,0,0,0,0,0};
int buf_avg[12]   = {0,0,0,0,0,0,0,0,0,0,0,0};
String consoleOutput;

void setup() {
  Serial.begin(9600);
  initIO();  
}

void initIO(){
  pinMode(0, INPUT);
  pinMode(1, INPUT);
  pinMode(2, INPUT);
  pinMode(3, INPUT);
  pinMode(4, INPUT);
  pinMode(5, INPUT);
  pinMode(6, INPUT);  
  pinMode(7, INPUT);
  pinMode(26, INPUT);
  pinMode(27, INPUT);
  pinMode(28, INPUT);
  pinMode(29, INPUT);
  pinMode(30, INPUT);
  pinMode(31, INPUT);  
  pinMode(14, OUTPUT); //ANALOG OUTPUT
}

void meanFilterInput(){
  //record analog input, 13 and 14 are binary, so no averaging is required
  buf_0[buf_index[0]]   = analogRead(0);  //left finger
  buf_1[buf_index[1]]   = analogRead(1);  //left upper palm
  buf_2[buf_index[2]]   = analogRead(2);  //right finger
  buf_3[buf_index[3]]   = analogRead(3);  //right upper palm
  buf_4[buf_index[4]]   = analogRead(4);  //right lower palm
  buf_5[buf_index[5]]   = analogRead(5);  //left lower palm
  buf_6[buf_index[6]]   = analogRead(26); //accel_1_x_neck
  buf_7[buf_index[7]]   = analogRead(27); //accel_1_y_neck
  buf_8[buf_index[8]]   = analogRead(28); //accel_1_z_neck
  buf_9[buf_index[9]]   = analogRead(29); //accel_2_x_wrist
  buf_10[buf_index[10]] = analogRead(30); //accel_2_y_wrist
  buf_11[buf_index[11]] = analogRead(31); //accel_2_z_wrist
  val_12                = analogRead(7);  //tilt_short_upper
  val_13                = analogRead(6);  //tilt_tall_lower

  //update buffer indices
  buf_index[0] = (buf_index[0] + 1) % BUF_SIZE;
  buf_index[1] = (buf_index[1] + 1) % BUF_SIZE;
  buf_index[2] = (buf_index[2] + 1) % BUF_SIZE;
  buf_index[3] = (buf_index[3] + 1) % BUF_SIZE;
  buf_index[4] = (buf_index[4] + 1) % BUF_SIZE;
  buf_index[5] = (buf_index[5] + 1) % BUF_SIZE;
  buf_index[6] = (buf_index[6] + 1) % BUF_SIZE;
  buf_index[7] = (buf_index[7] + 1) % BUF_SIZE;
  buf_index[8] = (buf_index[8] + 1) % BUF_SIZE;
  buf_index[9] = (buf_index[9] + 1) % BUF_SIZE;
  buf_index[10] = (buf_index[10] + 1) % BUF_SIZE;
  buf_index[11] = (buf_index[11] + 1) % BUF_SIZE;

  //sum up current sensor value with past values
  for (int i = 0; i < BUF_SIZE; ++i){      
    buf_sum[0] = buf_sum[0] + buf_0[i];
    buf_sum[1] = buf_sum[1] + buf_1[i];
    buf_sum[2] = buf_sum[2] + buf_2[i];
    buf_sum[3] = buf_sum[3] + buf_3[i];
    buf_sum[4] = buf_sum[4] + buf_4[i];
    buf_sum[5] = buf_sum[5] + buf_5[i];
    buf_sum[6] = buf_sum[6] + buf_6[i];
    buf_sum[7] = buf_sum[7] + buf_7[i];
    buf_sum[8] = buf_sum[8] + buf_8[i];
    buf_sum[9] = buf_sum[9] + buf_9[i];
    buf_sum[10] = buf_sum[10] + buf_10[i];
    buf_sum[11] = buf_sum[11] + buf_11[i];      
  }

  //calculate the average
  for (int i = 0; i < 12; ++i){    
    buf_avg[i] = buf_sum[i]/BUF_SIZE;      
  } 
}

void resetBuf(){
  for (int i = 0; i < 12; ++i){
    buf_sum[i] = 0;
    buf_avg[i] = 0;
  }
}

void mapSensors(){
  leftPressureFinger  = buf_avg[0];
  leftPressureUpper   = buf_avg[1];
  rightPressureFinger = buf_avg[2];
  rightPressureUpper  = buf_avg[3];
  rightPressureLower  = buf_avg[4];
  leftPressureLower   = buf_avg[5];    
  neckAccelX          = buf_avg[6];   //accel_1_x
  neckAccelY          = buf_avg[7];   //accel_1_y
  neckAccelZ          = buf_avg[8];   //accel_1_z
  wristAccelX         = buf_avg[9];   //accel_2_x
  wristAccelY         = buf_avg[10];  //accel_2_y
  wristAccelZ         = buf_avg[11];  //accel_2_z
  tiltUpperBack       = val_12;       //tilt_short
  tiltLowerBack       = val_13;       //tilt_tall
}


void checkTime(){
  if (!heavy_lift){
    heavy_lift = true;
    start = millis();
  }
  end = millis();
  
  if ((end - start) >= MAX_LIFT_DURATION){
    triggerHaptic();
  }  
  Serial.println("Elapsed time (ms): " + String(end-start));
}

void checkLift(){
  load_type = scale.estimateWeight(leftPressureUpper, leftPressureLower, leftPressureFinger, rightPressureUpper, rightPressureLower, rightPressureFinger);
    
  if (load_type == LIGHT){
    heavy_lift = false;    
    detector.clearImpulseState();
    stopHaptic();    
  }
  else if (load_type == HEAVY){
    //check lift duration
    checkTime();    
    
    //check for incorrect posture indicators    
    elevation_type = detector.checkArmElevation(wristAccelX, wristAccelY, wristAccelZ);
    twist_type = detector.checkImpulse(wristAccelX, wristAccelY, wristAccelZ);
    backtilt_type = detector.checkBackTilt(tiltUpperBack, tiltLowerBack);

    //if any indicators of incorrect posture are found, trigger warning
    if((elevation_type == HIGH_ELEV) || (twist_type == TWIST) || (backtilt_type == FULLY_BENT)){
      triggerHaptic();
    }
    else{
      stopHaptic();      
    }
  }  else if (load_type == OVERLOAD){
    heavy_lift = false;
    detector.clearImpulseState();
    triggerHaptic();
  }  
}

void triggerHaptic(){
  //Serial.println("Triggered haptic feedback");
  analogWrite(14, 255);
}

void stopHaptic(){
  //Serial.println("Stopped haptic feedback");
  analogWrite(14, 0);
}

void loop() {
  meanFilterInput(); 
  mapSensors();  

  //console
  //Serial.println(String(neckAccelX) + ", " + String(neckAccelY) + ", " + String(neckAccelZ));
  //Serial.println(String(wristAccelX) + ", " + String(wristAccelY) + ", " + String(wristAccelZ));
  //Serial.println(String(tiltUpperBack) + ", " + String(tiltLowerBack));
  //Serial.println(String(leftPressureUpper) + ", " + String(leftPressureLower) + ", " + String(leftPressureFinger) + ", " + String(rightPressureUpper) + ", " + String(rightPressureLower) + ", " + String(rightPressureFinger));

  checkLift();     
  
  //debug
  //twist_type = detector.checkImpulse(neckAccelX, neckAccelY, neckAccelZ, wristAccelX, wristAccelY, wristAccelZ);
  //elevation_type = detector.checkArmElevation(wristAccelX, wristAccelY, wristAccelZ);
  //backtilt_type = detector.checkBackTilt(tiltUpperBack, tiltLowerBack);
  //load_type = scale.estimateWeight(leftPressureUpper, leftPressureLower, leftPressureFinger, rightPressureUpper, rightPressureLower, rightPressureFinger);
  //triggerHaptic();
  //printConsole();
         
  resetBuf(); 
  delay(POLL_DELAY);
}


void printConsole(){  
  for (int i = 0; i < 12; ++i){
    consoleOutput += String(buf_avg[i]) + ", ";
  }
  consoleOutput = consoleOutput + String(val_12) + ", " + String(val_13);
  Serial.println(consoleOutput);
  consoleOutput = "";
}

