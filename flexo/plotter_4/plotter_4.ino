int buffer_1 [10];
int buffer_2 [10];
int buffer_3 [10];
int buffer_4 [10];

int index[4] = {0,0,0,0};

int avg1 = 0;
int avg2 = 0;
int avg3 = 0;
int avg4 = 0;



void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  pinMode(1, INPUT);
  pinMode(2, INPUT);
  pinMode(3, INPUT);
  pinMode(4, INPUT);  
}

void updateBuffer(){
  buffer_1[index[0]] = analogRead(1);
  buffer_2[index[1]] = analogRead(2);
  buffer_3[index[2]] = analogRead(3);
  buffer_4[index[3]] = analogRead(4);

  index[0] = (index[0] + 1) % 10;
  index[1] = (index[1] + 1) % 10;
  index[2] = (index[2] + 1) % 10;
  index[3] = (index[3] + 1) % 10;  
}

void calAvg(){
    for (int i = 0; i < 10; i ++){
    avg1 = avg1 + buffer_1[i];
    avg2 = avg2 + buffer_2[i];
    avg3 = avg3 + buffer_3[i];
    avg4 = avg4 + buffer_4[i];
  }

  if (avg1 != 0){
    avg1 = avg1/10;
  }

  if (avg2 != 0){
    avg2 = avg2/10;
  }

  if (avg3 != 0){
    avg3 = avg3/10;
  }

  if (avg4 != 0){
    avg4 = avg4/10;
  }
  
}

void loop() {

  updateBuffer();
  calAvg();  
  
  Serial.print(500);
  Serial.print("  ");
  Serial.print(0);
  Serial.print("  ");

  Serial.print(avg1);
  Serial.print("  ");
  Serial.print(avg2);
  Serial.print("  ");
  Serial.print(avg3);
  Serial.print("  ");
  Serial.println(avg4);

  avg1 = 0;
  avg2 = 0;
  avg3 = 0;
  avg4 = 0;

  delay(50);
}
