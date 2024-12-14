int relayPin = 4;  // Pin connected to the relay's SIG pin

void setup() {
  pinMode(relayPin, OUTPUT); // Set relay pin as output
  digitalWrite(relayPin, HIGH); // Ensure relay is initially off
  delay(100); // Small delay for stability
  digitalWrite(relayPin, LOW);  // Turn relay on
  delay(2000); // Wait 1 second
  digitalWrite(relayPin, HIGH); // Turn relay off
}

void loop() {
  // Do nothing (stops here)
}