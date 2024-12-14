package com.project.sproutling.utils

class MockArduinoConnection : ArduinoConnection("mock", 0) {
    private val responses = mutableMapOf<String, String>()
    private var lastCommand: String? = null
    private var lastResponse: String? = null

    init {
        // Predefined responses
        responses["start_watering"] = """{"status": "success", "action":"watered"}"""
        responses["moisture_update"] = """{"moisture": 45, "lastWatered":"2024-11-23T14:00:00"}"""
    }

    override fun sendJson(data: String) {
        println("MockArduinoConnection: Sent -> $data")
        try {
            // Parse the JSON string to extract the "command" field
            val jsonObject = org.json.JSONObject(data)
            lastCommand = jsonObject.optString("command", null.toString()) // Safely get "command"
            println("MockArduinoConnection: Parsed command -> $lastCommand")
        } catch (e: Exception) {
            println("MockArduinoConnection: Error parsing command -> $e")
            lastCommand = null // Reset command if parsing fails
        }
    }

    /*@RequiresApi(Build.VERSION_CODES.N)
    override fun receiveJson(): String {
        println("MockArduinoConnection: Receiving mock data...")
        println("MockArduinoConnection: Last command -> $lastCommand")
        return responses.getOrDefault(lastCommand, """{"status": "error"}""")
    }*/
}