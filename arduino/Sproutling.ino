#include <BetterWiFiNINA.h>
#include <ArduinoJson.h>
#include <Bounce2.h>

// WiFi Init
const char ssid[] = "Yahaha! You found me!";
const char pass[] = "arduino1";

int status = WL_IDLE_STATUS;

IPAddress ip;
WiFiSocket clientSocket;
WiFiSocket serverSocket;


/* ------------- START CONFIG ------------- */
constexpr int RELAY_PIN = 4;
constexpr int MOIST_PIN = A0;

unsigned long wateringDuration = 2000;  // 2 seconds in milliseconds
bool wateringComplete = false;          // New flag to track watering completion
int raw_moisture = 0;
int moisture;
/* ------------- END CONFIG ------------- */
bool watering = false;
unsigned long startedWatering;

void setup() {
  // Initialize serial and wait for port to open:

  Serial.begin(9600);
  Serial.println("");
  //for(unsigned long const serialBeginTime = millis(); !Serial && (millis() - serialBeginTime <= 5000); ) { }


  /* * * * * * * * * * * * * * * * * * HARDWARE SETUP * * * * * * * * * * * * * * * */
  pinMode(RELAY_PIN, OUTPUT);

  // Make sure the pump is not running
  stopWatering();

  // Blink LED to confirm we're up and running
  for (int i = 0; i <= 4; i++) {
    digitalWrite(LED_BUILTIN, HIGH);
    delay(200);
    digitalWrite(LED_BUILTIN, LOW);
    delay(200);
  }

  setUpWiFi();
}

void setUpWiFi() {
  // Check for the WiFi module:
  if (WiFi.status() == WL_NO_MODULE) {
    Serial.println("Communication with WiFi module failed!");
    while (true) {
      // Error pattern: Short-Short-Short blink (SOS-like)
      for (int i = 0; i < 3; i++) {
        digitalWrite(LED_BUILTIN, HIGH);
        delay(200);
        digitalWrite(LED_BUILTIN, LOW);
        delay(200);
      }
      delay(1000);
    }
  }

  Serial.println("WiFi module found!");

  WiFi.disconnect();
  delay(1000);

  int attempts = 0;
  const int MAX_ATTEMPTS = 5;

  while (attempts < MAX_ATTEMPTS) {
    Serial.print("\nAttempt ");
    Serial.print(attempts + 1);
    Serial.println("/5");

    // Rapid blink while attempting to connect
    digitalWrite(LED_BUILTIN, HIGH);
    delay(50);
    digitalWrite(LED_BUILTIN, LOW);
    delay(50);

    status = WiFi.begin(ssid, pass);

    unsigned long startTime = millis();
    while (WiFi.status() != WL_CONNECTED && (millis() - startTime < 10000)) {
      // Quick blink during connection attempt
      digitalWrite(LED_BUILTIN, HIGH);
      delay(50);
      digitalWrite(LED_BUILTIN, LOW);
      delay(450);
      Serial.print(".");
      if (millis() - startTime > 0 && (millis() - startTime) % 1000 == 0) {
        Serial.print("\nCurrent WiFi Status: ");
        Serial.println(WiFi.status());
      }
    }

    if (WiFi.status() == WL_CONNECTED) {
      Serial.println("\nConnected successfully!");
      // Success pattern: Two long blinks
      for (int i = 0; i < 2; i++) {
        digitalWrite(LED_BUILTIN, HIGH);
        delay(1000);
        digitalWrite(LED_BUILTIN, LOW);
        delay(500);
      }
      printWiFiStatus();
      return;
    } else {
      Serial.print("\nConnection failed with status: ");
      Serial.println(WiFi.status());
      attempts++;
      // Failed attempt pattern: three quick blinks
      for (int i = 0; i < 3; i++) {
        digitalWrite(LED_BUILTIN, HIGH);
        delay(100);
        digitalWrite(LED_BUILTIN, LOW);
        delay(100);
      }
      delay(1000);
    }
  }

  // If we get here, all attempts failed
  // Continuous error pattern in loop()
  while (true) {
    // SOS pattern
    for (int i = 0; i < 3; i++) {
      digitalWrite(LED_BUILTIN, HIGH);
      delay(200);
      digitalWrite(LED_BUILTIN, LOW);
      delay(200);
    }
    delay(1000);
  }
}

// New function to reinitialize server socket
void reinitializeServerSocket() {
  // Close existing socket if open
  if (serverSocket) {
    serverSocket.close();
  }

  // Create server socket
  serverSocket = WiFiSocket(WiFiSocket::Type::Stream, WiFiSocket::Protocol::TCP);
  if (!serverSocket) {
    Serial.print("Creating server socket failed: error ");
    Serial.println(WiFiSocket::lastError());
    return;
  }

  // Bind to port
  if (!serverSocket.bind(8080)) {
    Serial.print("Binding server socket failed: error ");
    Serial.println(WiFiSocket::lastError());
    return;
  }

  // Set to non-blocking
  if (!serverSocket.setNonBlocking(true)) {
    Serial.println("Setting server socket to non-blocking failed: error ");
    Serial.println(WiFiSocket::lastError());
    return;
  }

  // Start listening
  if (!serverSocket.listen(5)) {
    Serial.print("Listen on server socket failed: error ");
    Serial.println(WiFiSocket::lastError());
    return;
  }

  Serial.println("Server socket reinitialized successfully");
}

void loop() {
  /* * * * * * * * * * * * * * * * WIFI CONNECTION HANDLING * * * * * * * * * * * * * * * */
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("WiFi connection lost. Attempting to reconnect...");
    setUpWiFi();

    if (WiFi.status() != WL_CONNECTED) {
      delay(30000);  // Wait 30 seconds before retry
    }
    return;
  }

  // Handle watering status first
  unsigned long currentMillis = millis();
  if (watering && (currentMillis - startedWatering >= wateringDuration)) {
    stopWatering();
  }

  static unsigned long lastConnectionCheck = 0;
  const unsigned long CONNECTION_CHECK_INTERVAL = 1000;  // Check every second

  if (currentMillis - lastConnectionCheck >= CONNECTION_CHECK_INTERVAL) {
    lastConnectionCheck = currentMillis;
    acceptConnectionIfPossible();
  }

  if (clientSocket) {
    readJsonData();
  }

  /* * * * * * * * * * * * * * * * * HARDWARE MANAGEMENT * * * * * * * * * * * * * * * * */
  raw_moisture = analogRead(MOIST_PIN);
  moisture = map(raw_moisture, 900, 400, 0, 100);
}

// This function is triggered whenever the server sends a change event,
// which means that someone changed a value remotely and we need to do
// something.
void onWateringChange() {
  if (watering) {
    startWatering();
  } else {
    stopWatering();
  }
}

void startWatering() {
  watering = true;
  wateringComplete = false;  // Reset completion flag
  startedWatering = millis();
  digitalWrite(RELAY_PIN, HIGH);  // Turn on pump

  // Send immediate success response for the command
  StaticJsonDocument<100> doc;
  doc["status"] = "success";
  sendJson(doc);
}

void stopWatering() {
  watering = false;
  wateringComplete = true;  // Set completion flag
  digitalWrite(RELAY_PIN, LOW);
}

void acceptConnectionIfPossible() {
  if (!serverSocket) {
    Serial.println("Server socket invalid - reinitializing...");
    reinitializeServerSocket();
    return;
  }

  if (clientSocket) {
    return;  // Skip the verbose message, just return if we have a client
  }

  // Accept incoming connection
  IPAddress addr;
  uint16_t port;
  clientSocket = serverSocket.accept(addr, port);

  if (!clientSocket) {
    auto err = WiFiSocket::lastError();
    if (err != EWOULDBLOCK) {
      Serial.print("Accept on server socket failed: error ");
      Serial.println(err);
      Serial.println("Attempting to reinitialize server socket...");
      reinitializeServerSocket();
    }
    return;
  }

  if (!clientSocket.setNonBlocking(true)) {
    Serial.print("Setting socket to non-blocking failed: error ");
    Serial.println(WiFiSocket::lastError());
    return;
  }

  Serial.print("New client connected from IP: ");
  Serial.println(addr);
  Serial.print("Port: ");
  Serial.println(port);
}

void printWiFiStatus() {

  // print the SSID of the network you're attached to:
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  // print your board's IP address:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

  // print the received signal strength:
  long rssi = WiFi.RSSI();
  Serial.print("signal strength (RSSI):");
  Serial.print(rssi);
  Serial.println(" dBm");

  Serial.print("Subnet Mask: ");
  Serial.println(WiFi.subnetMask());
  Serial.print("Gateway IP: ");
  Serial.println(WiFi.gatewayIP());
}

void readJsonData() {
  char buffer[256];
  int read = clientSocket.recv(buffer, sizeof(buffer) - 1);

  if (read > 0) {
    buffer[read] = '\0';
    Serial.print("Received data from Android (");
    Serial.print(read);
    Serial.print(" bytes): ");
    Serial.println(buffer);

    StaticJsonDocument<200> doc;
    DeserializationError error = deserializeJson(doc, buffer);

    if (error) {
      Serial.print("JSON parse failed: ");
      Serial.println(error.f_str());
      return;
    }

    const char* command = doc["command"];
    Serial.print("Received command: ");
    Serial.println(command);

    if (strcmp(command, "moisture_update") == 0) {
      Serial.println("Handling moisture_update command...");
      sendMoistureUpdate(moisture);
      Serial.println("Moisture update sent.");
    } else if (strcmp(command, "water_plant") == 0) {
      Serial.println("Handling water_plant command...");
      if (!watering) {  // Only start watering if not already watering
        startWatering();
      }
    } else {
      Serial.print("Unknown command: ");
      Serial.println(command);
    }
  } else if (read < 0) {
    auto err = WiFiSocket::lastError();
    if (err != EAGAIN) {
      Serial.print("Error reading from client: ");
      Serial.println(err);

      if (err == ENOTCONN || err == EBADF) {
        Serial.println("Client disconnected, cleaning up socket");
        clientSocket.close();
        clientSocket = WiFiSocket();
        reinitializeServerSocket();
      }
    }
  }
}

// Modify sendJson to add newline
void sendJson(StaticJsonDocument<100>& doc) {
  String jsonString;
  serializeJson(doc, jsonString);
  jsonString += "\n";  // Add newline character

  if (clientSocket) {
    int sent = clientSocket.send(jsonString.c_str(), jsonString.length());
    if (sent < 0) {
      Serial.print("Failed to send JSON, error: ");
      Serial.println(WiFiSocket::lastError());
    }
  }
}

void sendWateringStatus(bool success) {
  StaticJsonDocument<100> doc;
  doc["success"] = success ? "success" : "failure";

  sendJson(doc);
}

// Update the moisture level for a plant
void sendMoistureUpdate(double moistureValue) {
  StaticJsonDocument<100> doc;
  doc["moisture"] = moistureValue;

  String jsonString;
  serializeJson(doc, jsonString);
  jsonString += "\n";  // Add newline

  Serial.print("Attempting to send moisture value: ");
  Serial.print(moistureValue);
  Serial.print(" as JSON: ");
  Serial.println(jsonString);

  int sent = clientSocket.send(jsonString.c_str(), jsonString.length());
  if (sent < 0) {
    Serial.print("Failed to send moisture update, error: ");
    Serial.println(WiFiSocket::lastError());
  } else {
    Serial.print("Successfully sent moisture update: ");
    Serial.print(sent);
    Serial.println(" bytes");
  }
}