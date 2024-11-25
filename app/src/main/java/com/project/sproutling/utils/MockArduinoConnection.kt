package com.project.sproutling.utils

import android.os.Build
import androidx.annotation.RequiresApi

class MockArduinoConnection : ArduinoConnection("mock", 0) {
    private val responses = mutableMapOf<String, String>()

    init {
        // Predefined responses
        responses["start_watering"] = """{"status": "success","action":"watered"}"""
        responses["moisture_update"] = """{"moisture": 45,"lastWatered":"2024-11-23T14:00:00"}"""
    }

    override fun sendJson(data: String) {
        println("MockArduinoConnection: Sent -> $data")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun receiveJson(): String {
        println("MockArduinoConnection: Receiving mock data...")
        // Simulate an action based on the last sent command
        return responses.getOrDefault("STATUS", """{"status":"error"}""")
    }
}