#include <ESP8266WiFi.h>
#include <WebSocketsServer.h>
#include <ArduinoBLE.h>

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

BLEService bluetoothService("75cececa-bf0a-11ed-a712-f7b97125a3fb");
BLEWordCharacteristic networkName("c2f0", BLERead);
BLEWordCharacteristic networkPassword("da94", BLERead);
BLELongCharacteristic handshake("e42f", BLERead, BLEWrite);

const byte[1] manufacturerData = { 0x45 };

const int TIMEOUT_BLUETOOTH_INIT = 100000;

void setup() {
  Serial.begin(115200);
  while (!Serial);
  Serial.println("START");
  connect();
  for (int8_t i = 0; i < 4; i++) pinMode(rightMotorPins[i], OUTPUT);
  for (int8_t i = 0; i < 4; i++) pinMode(leftMotorPins[i], OUTPUT);

  // Setup BLE
  long start = millis();
  while(!BLE.begin()) {
    Serial.println("Initializing Bluetooth...");
    if (start + TIMEOUT_BLUETOOTH_INIT < millis()) {
      Serial.println("TIMEOUT");
      // TODO : Restart device
    }
  }

  BLE.setDeviceName("Jerry");
  BLE.setLocalName("Jerry");
  BLE.setAppearance(0x08C0); // Generic Motorized Vehicle - https://btprodspecificationrefs.blob.core.windows.net/assigned-numbers/Assigned%20Number%20Types/Assigned%20Numbers.pdf
  BLE.setAdvertisedServiceUuid("60e00f22bf0711edbe81eb2e89a31848");
  BLE.setManufacturerData(manufacturerData, 1);

  BLE.setEventHandler(BLEConnected, onBluetoothConnect);
  BLE.setEventHandler(BLEDisconnected, onBluetoothDisconnect);

  if (!networkName.canSubscribe()) {
    Serial.println("Cannot subscribe to network name");
  }
  if (!networkPassword.canSubscribe()) {
    Serial.println("Cannot subscribe to network name");
  }
  if (!handshake.canSubscribe()) {
    Serial.println("Cannot subscribe to network name");
  }
  
  networkName.setEventHandler(BLEWritten | BLESubscribed | BLEUnsubscribed | BLERead, onNetworkNameEvent);
  networkName.subscribe();
  bluetoothService.addCharacteristic(networkName);
  
  networkPassword.setEventHandler(BLEWritten | BLESubscribed | BLEUnsubscribed | BLERead, onNetworkPasswordEvent);
  networkPassword.subscribe();
  bluetoothService.addCharacteristic(networkPassword);
  
  handshake.setEventHandler(BLEWritten | BLESubscribed | BLEUnsubscribed | BLERead, onHandshakeEvent);
  handshake.subscribe();
  bluetoothService.addCharacteristic(networkName);

  BLE.addService(bluetoothService);

  BLE.advertise();
}

void onNetworkNameEvent(BLEDevice device, BLEWordCharacteristic characteristic) {
  Serial.print("Recieved network name: ");
  Serial.println(device.address());
  Serial.println(characteristic.valueUpdated());
  Serial.println(characteristic.valueLength());
  Serial.println(characteristic.valueSize());
  Serial.println(characteristic.value());
}

void onNetworkPasswordEvent(BLEDevice device, BLEWordCharacteristic characteristic) {
  Serial.print("Recieved network password: ");
  Serial.println(device.address());
  Serial.println(characteristic.valueUpdated());
  Serial.println(characteristic.valueLength());
  Serial.println(characteristic.valueSize());
  Serial.println(characteristic.value());
}

void onHandshakeEvent(BLEDevice device, BLELongCharacteristic characteristic) {
  Serial.println("Subscribed to handshake");
  Serial.println(device.address());
  Serial.println(characteristic.valueUpdated());
  Serial.println(characteristic.valueLength());
  Serial.println(characteristic.valueSize());
  Serial.println(characteristic.value());
}

void onBluetoothConnect(BLEDevice device) {
  Serial.print("Connected to a device: ");
  Serial.println(device.address());
}

void onBluetoothDisconnect(BLEDevice device) {
  Serial.print("Disconnected from device: ");
  Serial.println(device.address());
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
