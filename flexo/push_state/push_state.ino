/*
  Blink
  Turns on an LED on for one second, then off for one second, repeatedly.

  Most Arduinos have an on-board LED you can control. On the Uno and
  Leonardo, it is attached to digital pin 13. If you're unsure what
  pin the on-board LED is connected to on your Arduino model, check
  the documentation at http://www.arduino.cc

  This example code is in the public domain.

  modified 8 May 2014
  by Scott Fitzgerald
 */

int current_state = 0;
int input_pin2;
int ignore_flag = 0;

// the setup function runs once when you press reset or power the board
void setup() {
  Serial.begin(9600);
  pinMode(2, INPUT);
}

// the loop function runs over and over again forever
void loop() {
  input_pin2 = digitalRead(2);

    
  if (ignore_flag == 0 && input_pin2 == 0){
        current_state = current_state + 1; //not limited to 255 (assume classic int)
        ignore_flag = 1;

        Serial.print("State Value: ");
        Serial.print(current_state);
        Serial.print("\n");
        delay(100); //need a delay otherwise the input sometimes gets triggered twice
  }

  if (ignore_flag == 1 && input_pin2 == 1){
    ignore_flag = 0;
  }

  
  //inputs are not queued, so avoid using delays as much as possible
}
