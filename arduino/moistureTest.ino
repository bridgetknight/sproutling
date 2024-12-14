constexpr int MOIST_PIN  = A0;

void setup() {
  // Initialize serial communication for debugging
  Serial.begin(9600);
  Serial.println("Starting moisture sensor calibration...");
}

void loop() {
  // Read the moisture sensor value (raw value between 0 and 1023)
  int rawMoisture = analogRead(MOIST_PIN);

  // Print the raw moisture reading to the Serial Monitor for calibration
  Serial.print("Raw moisture reading: ");
  Serial.println(rawMoisture);

  // Optional: Map the value to a percentage (0 = dry, 100 = wet)
  int moisture = map(rawMoisture, 1023, 400, 0, 100); // Adjust these numbers based on your sensor's calibration
  Serial.print("Moisture percentage: ");
  Serial.println(moisture);

  // Delay to avoid flooding the Serial Monitor with data too quickly
  delay(1000);  // Update every second
}