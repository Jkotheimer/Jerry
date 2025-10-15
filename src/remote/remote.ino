#ifndef RIGHT_MOTOR

// Motor pin definitions
#define RIGHT_FORWARD 7
#define RIGHT_BACKWARD 8

#endif

float oneSecond = 1000000.0;
float frequency = 500.0;
float dutyCycle = 0.990;

uint8_t rightMotorSpeed = 0;
uint8_t leftMotorSpeed = 0;


void setup() {
  Serial.begin(115200);
  Serial.println("START");

  pinMode(RIGHT_FORWARD, OUTPUT);
  pinMode(RIGHT_BACKWARD, OUTPUT);

}

void loop() {
  for (int i = 0; i < 512; i++ ) {
    Serial.print("Analog Value: ");
    Serial.println(255);
    analogWrite(RIGHT_FORWARD, 255);
    analogWrite(RIGHT_BACKWARD, 0);
    delay(1000);
  }
}
