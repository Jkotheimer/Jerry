#ifndef FORWARD

#define FORWARD 1
#define REVERSE -1
#define STOP 0

// Motor pin definitions
#define P1 10
#define P2 9
#define P3 8
#define P4 7

#define STEP_DELAY 3

#endif

const uint8_t stepSequence[32] = {
  1,0,0,0,
  1,1,0,0,
  0,1,0,0,
  0,1,1,0,
  0,0,1,0,
  0,0,1,1,
  0,0,0,1,
  1,0,0,1
};

void setup() {
  Serial.begin(115200);
  Serial.println("START");
  
  pinMode(P1, OUTPUT);
  pinMode(P2, OUTPUT);
  pinMode(P3, OUTPUT);
  pinMode(P4, OUTPUT);
}

void loop() {
  setMotorState(FORWARD);
}

void setMotorState(int direction) {
  Serial.print("Setting motor state: ");
  Serial.println(direction);
  if (direction == FORWARD) clockwiseStep();
  else if (direction == REVERSE) ctrclockwiseStep();
}


void clockwiseStep() {
  for (int8_t i = 0; i < sizeof(stepSequence);) {
    writeMotorPins(stepSequence[i++], stepSequence[i++], stepSequence[i++], stepSequence[i++]);
    delay(STEP_DELAY);
  }
}

void ctrclockwiseStep() {
  for (int8_t i = sizeof(stepSequence); i > -1; i-=4) {
    writeMotorPins(stepSequence[i-3], stepSequence[i-2], stepSequence[i-1], stepSequence[i]);
    delay(STEP_DELAY);
  }
}

void writeMotorPins(int a, int b, int c, int d) {
  digitalWrite(P1, a);
  digitalWrite(P2 ,b);
  digitalWrite(P3, c);
  digitalWrite(P4, d);
}
