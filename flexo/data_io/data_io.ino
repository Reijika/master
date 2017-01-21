const int BUF_SIZE = 10;
const int DELAY = 100;

// INPUT BUFFERS
int buf_1 [BUF_SIZE];     //left hand - pressure sensor 1
int buf_2 [BUF_SIZE];     //left hand - pressure sensor 2
int buf_3 [BUF_SIZE];     //left hand - pressure sensor 3
int buf_4 [BUF_SIZE];     //right hand - pressure sensor 1
int buf_5 [BUF_SIZE];     //right hand - pressure sensor 2
int buf_6 [BUF_SIZE];     //right hand - pressure sensor 3
int buf_7 [BUF_SIZE];     //neck - accelerometer x
int buf_8 [BUF_SIZE];     //neck - accelerometer y
int buf_9 [BUF_SIZE];     //neck - accelerometer z
int buf_10 [BUF_SIZE];    //wrist - accelerometer x
int buf_11 [BUF_SIZE];    //wrist - accelerometer y
int buf_12 [BUF_SIZE];    //wrist - accelerometer z
int val_13 = 0;           //upper back - tilt sensor 1
int val_14 = 0;           //lower back - tilt sensor 2

//input index and avg buffers
int buf_index[12] = {0,0,0,0,0,0,0,0,0,0,0,0};
int buf_sum[12] = {0,0,0,0,0,0,0,0,0,0,0,0};
int buf_avg[12] = {0,0,0,0,0,0,0,0,0,0,0,0};

void setup() {
  Serial.begin(9600);
  pinMode(0, INPUT);
  pinMode(1, INPUT);
  pinMode(2, INPUT);
  pinMode(3, INPUT);
  pinMode(4, INPUT);
  pinMode(5, INPUT);
  pinMode(6, INPUT);
  pinMode(7, INPUT);
  pinMode(8, INPUT);
  pinMode(9, INPUT);
  pinMode(10, INPUT);
  pinMode(11, INPUT);
  pinMode(12, INPUT);
  pinMode(13, INPUT);  
  pinMode(A14, OUTPUT);
}

void updateBuffer(){
  //record analog input, 13 and 14 are binary, so no averaging is required
  buf_1[buf_index[0]] = analogRead(0);
  buf_2[buf_index[1]] = analogRead(1);
  buf_3[buf_index[2]] = analogRead(2);
  buf_4[buf_index[3]] = analogRead(3);
  buf_5[buf_index[4]] = analogRead(4);
  buf_6[buf_index[5]] = analogRead(5);
  buf_7[buf_index[6]] = analogRead(6);
  buf_8[buf_index[7]] = analogRead(7);
  buf_9[buf_index[8]] = analogRead(8);
  buf_10[buf_index[9]] = analogRead(9);
  buf_11[buf_index[10]] = analogRead(10);
  buf_12[buf_index[11]] = analogRead(11);
  val_13 = analogRead(12);
  val_14 = analogRead(13);

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
}

void calculateAvg(){
    for (int i = 0; i < BUF_SIZE; ++i){      
      buf_sum[0] = buf_sum[0] + buf_1[i];
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
      buf_sum[12] = buf_sum[12] + buf_12[i];      
  }

  for (int i = 0; i < 12; ++i){
    if (buf_sum[i] != 0){
      buf_avg[i] = buf_sum[i]/BUF_SIZE;  
    }    
  }  
}

void resetBuf(){
  for (int i = 0; i < 12; ++i){
    buf_sum[i] = 0;
    buf_avg[i] = 0;
  }
}

void printPlot(){
  Serial.print(500);
  Serial.print("  ");
  Serial.print(0);
  Serial.print("  ");

  Serial.print(buf_avg[0]);
  Serial.print("  ");
  Serial.print(buf_avg[1]);
  Serial.print("  ");
  Serial.print(buf_avg[2]);
  Serial.print("  ");
  Serial.print(buf_avg[3]);
  Serial.print("  ");
  Serial.print(buf_avg[4]);
  Serial.print("  ");
  Serial.print(buf_avg[5]);
  Serial.print("  ");
  Serial.print(buf_avg[6]);
  Serial.print("  ");
  Serial.print(buf_avg[7]);
  Serial.print("  ");
  Serial.print(buf_avg[8]);
  Serial.print("  ");
  Serial.print(buf_avg[9]);
  Serial.print("  ");
  Serial.print(buf_avg[10]);
  Serial.print("  ");
  Serial.print(buf_avg[11]);
  Serial.print("  ");
  Serial.print(val_13*500);
  Serial.print("  ");
  Serial.println(val_14*500);  
}

void loop() {

  updateBuffer();
  calculateAvg(); 
  printPlot();
  resetBuf(); 

  if (val_13 || val_14){
    analogWrite(A14, 255);
  }

  delay(DELAY);
}

//future reference: https://www.arduino.cc/en/reference/SD

