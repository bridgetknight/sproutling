package com.project.sproutling.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class NetworkScanner(private val context: Context) {
    private val prefs = context.getSharedPreferences("arduino_prefs", Context.MODE_PRIVATE)

    suspend fun findArduino(port: Int = 8080): String? {
        return withContext(Dispatchers.IO) {
            try {
                // First try last known subnet if we have one
                val lastSubnet = prefs.getString("last_arduino_subnet", null)
                if (lastSubnet != null) {
                    Log.d("NetworkScanner", "Trying last known subnet: $lastSubnet.*")
                    val ipInLastSubnet = scanSubnet(lastSubnet, port)
                    if (ipInLastSubnet != null) {
                        return@withContext ipInLastSubnet
                    }
                }

                // If that fails, scan common subnets first
                val commonSubnets = listOf(
                    "192.168.171",  // Your current Arduino subnet
                    "192.168.172",  // Adjacent subnet
                    "192.168.170"   // Adjacent subnet
                )

                for (subnet in commonSubnets) {
                    Log.d("NetworkScanner", "Scanning subnet: $subnet.*")
                    val ip = scanSubnet(subnet, port)
                    if (ip != null) {
                        // Save the successful subnet for next time
                        prefs.edit().putString("last_arduino_subnet", subnet).apply()
                        return@withContext ip
                    }
                }

                null
            } catch (e: Exception) {
                Log.e("NetworkScanner", "Error scanning network: ${e.message}")
                null
            }
        }
    }

    private suspend fun scanSubnet(subnet: String, port: Int): String? {
        return withContext(Dispatchers.IO) {
            val jobs = (2..254).map { lastOctet ->
                async {
                    val ip = "$subnet.$lastOctet"
                    if (testArduinoConnection(ip, port)) {
                        ip
                    } else {
                        null
                    }
                }
            }

            jobs.awaitAll().firstNotNullOfOrNull { it }
        }
    }

    private suspend fun testArduinoConnection(ip: String, port: Int): Boolean {
        return try {
            withTimeout(300) { // 300ms timeout for each attempt
                val connection = ArduinoConnection(ip, port)
                connection.open()

                // Send a test request
                connection.sendJson("""{"command":"moisture_update"}""")
                val response = connection.receiveJson()

                connection.close()
                response != null
            }
        } catch (e: Exception) {
            false
        }
    }
}