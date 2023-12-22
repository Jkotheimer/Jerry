#include <ESP8266WiFi.h>
#include <WebSocketsServer.h>

#ifndef STEP_DELAY

// These are the indexes of the respective motor's state in the payload string (which will only be 2 bytes long)
#define LEFT 0
#define RIGHT 1

// The websocket payload sends state as a char '0', '1', or '2' for each motor. These are the ascii values.
#define REVERSE 48 // '0'
#define STOP 49    // '1'
#define FORWARD 50 // '2'

#define STEP_DELAY 2

#define CONNECT_INTERVAL 1000
#define CONNECT_TIMEOUT 5000

#endif

// MOTOR VARIABLES
const uint8_t rightMotorPins[4] = { D5, D6, D7, D8 };
const uint8_t leftMotorPins[4] = { D0, D1, D2, D3 };
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

int8_t leftMotorState = STOP;
int8_t rightMotorState = STOP;
bool isRotating = false;

// INTERNET VARIABLES
const String ssid = "********";
const String password = "********";
bool isConnected = false;
int numRetries = 0;

WebSocketsServer socket(80);
WiFiClient client;

void setup() {
  Serial.begin(115200);
  while (!Serial);
  Serial.println("START");
  connect();
  for (int8_t i = 0; i < 4; i++) pinMode(rightMotorPins[i], OUTPUT);
  for (int8_t i = 0; i < 4; i++) pinMode(leftMotorPins[i], OUTPUT);
}

void loop() {
  socket.loop();
  setMotorStates();
}

void handleWebSocketEvent(uint8_t num, WStype_t type, uint8_t* payload, size_t length) {
  if (type == WStype_CONNECTED) {
    // TODO : Authenticate
  }
  if (type != WStype_TEXT || length != 2) return;
  Serial.print("PAYLOAD LEFT: ");
  Serial.println(payload[LEFT]);
  Serial.print("PAYLOAD RIGHT: ");
  Serial.println(payload[RIGHT]);
  leftMotorState = (int8_t)payload[LEFT];
  rightMotorState = (int8_t)payload[RIGHT];
}

void connect() {
  WiFi.mode(WIFI_STA);
  Serial.print("Searching for ");
  Serial.println(ssid);
  Serial.print("Try number ");
  Serial.println(numRetries);
  int numNetworks = WiFi.scanNetworks();
  for (int i = 0; i < numNetworks; i++) {
    if (WiFi.SSID(i) == ssid) {
      WiFi.begin(WiFi.SSID(i), password, WiFi.channel(i), WiFi.BSSID(i), true);
      Serial.print("Found ");
      Serial.println(WiFi.SSID(i));
      break;
    }
  }
  int elapsedTime = 0;
  while (WiFi.status() != WL_CONNECTED) {
    delay(CONNECT_INTERVAL);
    elapsedTime += CONNECT_INTERVAL;
    if (elapsedTime > CONNECT_TIMEOUT) {
      numRetries++;
      return connect();
    }
  }
  Serial.println("Connected");

  Serial.println(WiFi.localIP());
    
  socket.begin();
  socket.onEvent(handleWebSocketEvent);

  isConnected = true;
}

void setMotorStates() {
  setMotorState(LEFT, leftMotorState);
  setMotorState(RIGHT, rightMotorState);
}

void setMotorState(int8_t side, int8_t state) {
  if (state == STOP) return;
  Serial.print("Setting motor state: ");
  Serial.print(side);
  Serial.println(state);
  if (state == FORWARD) clockwiseStep(side);
  else if (state == REVERSE) ctrclockwiseStep(side);
}

void clockwiseStep(int8_t side) {
  isRotating = true;
  for (int8_t i = 0; i < sizeof(stepSequence);) {
    uint8_t step[4] = {stepSequence[i++], stepSequence[i++], stepSequence[i++], stepSequence[i++]};
    writeMotorPins(side, step);
    int startTime = millis();
    while (startTime + STEP_DELAY >= millis());
  }
  isRotating = false;
}

void ctrclockwiseStep(int8_t side) {
  isRotating = true;
  for (int8_t i = sizeof(stepSequence); i >= 0; i-=4) {
    uint8_t step[4] = {stepSequence[i-4], stepSequence[i-3], stepSequence[i-2], stepSequence[i-1]};
    writeMotorPins(side, step);
    int startTime = millis();
    while (startTime + STEP_DELAY >= millis());
  }
  isRotating = false;
}

void writeMotorPins(int8_t side, uint8_t values[4]) {
  if (side == LEFT) for (int8_t i = 0; i < 4; i++) digitalWrite(leftMotorPins[i], values[i]);
  else if (side == RIGHT) for (int8_t i = 0; i < 4; i++) digitalWrite(rightMotorPins[i], values[i]);
}