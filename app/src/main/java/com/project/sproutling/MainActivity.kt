package com.project.sproutling

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.project.sproutling.data.PlantStorage
import com.project.sproutling.screens.MainScreen
import com.project.sproutling.ui.theme.SproutlingTheme
import com.project.sproutling.utils.ArduinoConnection
import com.project.sproutling.utils.ConnectionState
import com.project.sproutling.utils.NetworkScanner
import com.project.sproutling.utils.RetryPolicy
import com.project.sproutling.utils.isNetworkAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException

@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : ComponentActivity() {
    private var connection: ArduinoConnection = ArduinoConnection("", 0) // Default initialization
    private val isTesting = false // Switch to false for actual Arduino connection
    private val retryPolicy = RetryPolicy()

    private val _connectionState = MutableStateFlow(ConnectionState.CONNECTING)
    private val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")

        /*if (isTesting) {
            connection = MockArduinoConnection()
        }*/

        if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS), 1)
        }

        setContent {
            SproutlingTheme {
                val currentConnectionState by connectionState.collectAsState()
                MainScreen(
                    onWaterPlant = { waterPlant() },
                    requestMoistureUpdate = { requestMoistureUpdate() },
                    updateStatus = updateStatus,
                    connectToArduino = { connectToArduino() },
                    connectionState = currentConnectionState
                    )
            }
        }
        if (!isTesting) {
            lifecycleScope.launch {
                val context: Context = this@MainActivity
                Log.d("MainActivity", "Checking network availability...")
                if (isNetworkAvailable(context)) {
                    Log.d("MainActivity", "Network is available, attempting to connect to Arduino...")
                    connectToArduino()
                } else {
                    Log.d("MainActivity", "Network is not available")
                    _connectionState.value = ConnectionState.OFFLINE
                }
            }
        }

    }

    private fun waterPlant() {
        lifecycleScope.launch(Dispatchers.IO) {
            retryPolicy.retry("water plant command") {
                try {
                    connection.sendJson("""{"command":"water_plant"}""")
                    val response = connection.receiveJson()
                    Log.d("MainActivity", "Response from Arduino: $response")

                    withContext(Dispatchers.Main) {
                        if (response?.get("status") == "success") {
                            true
                        } else {
                            throw IOException("Failed to water plant")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error during watering: ${e.message}")
                    throw e
                }
            }
        }
    }

    private fun requestMoistureUpdate(): Map<String, String>? {
        return runBlocking {
            withContext(Dispatchers.IO) {
                try {
                    Log.d("MainActivity", "Starting moisture update request")

                    // Check connection before sending
                    if (!connection.isConnected()) {
                        Log.d("MainActivity", "Connection lost, attempting to reconnect...")
                        connection.open()
                    }

                    connection.sendJson("""{"command":"moisture_update"}""")
                    Log.d("MainActivity", "Sent moisture update request, waiting for response...")

                    withTimeout(5000) { // 5 second timeout
                        val response = connection.receiveJson()
                        Log.d("MainActivity", "Received moisture response: $response")
                        response
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error in requestMoistureUpdate: ${e.message}")
                    e.printStackTrace()
                    null
                }
            }
        }
    }

    private val updateStatus: suspend (
        updateMoisture: (String) -> Unit,
        updateLastWatered: (String) -> Unit,
        plantStorage: PlantStorage
    ) -> Unit = { updateMoisture, updateLastWatered, plantStorage ->
        withContext(Dispatchers.IO) {
            Log.d("MainActivity", "updateStatus called")

            try {
                val isConnected = connection.isConnected()
                Log.d("MainActivity", "Arduino connection status: $isConnected")

                if (isConnected) {
                    val response = requestMoistureUpdate()
                    Log.d("MainActivity", "Moisture update response: $response")

                    if (response != null) {
                        val moistureValue = response["moisture"]
                        Log.d("MainActivity", "Received moisture value: $moistureValue")

                        withContext(Dispatchers.Main) {
                            val plants = plantStorage.getPlants()
                            if (plants.isNotEmpty()) {
                                moistureValue?.let {
                                    updateMoisture(it)
                                }
                                val lastWatered = plantStorage.getLastWatered(plants.first().name)
                                lastWatered?.let {
                                    updateLastWatered(it)
                                }
                            }
                            _connectionState.value = ConnectionState.CONNECTED
                        }
                    } else {
                        Log.e("MainActivity", "No response received from Arduino")
                        withContext(Dispatchers.Main) {
                            updateMoisture("Error")
                            updateLastWatered("Error")
                            _connectionState.value = ConnectionState.OFFLINE
                        }
                    }
                } else {
                    Log.d("MainActivity", "Not connected to Arduino")
                    withContext(Dispatchers.Main) {
                        updateMoisture("Offline")
                        updateLastWatered("Offline")
                        _connectionState.value = ConnectionState.OFFLINE
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in updateStatus: ${e.message}")
                withContext(Dispatchers.Main) {
                    updateMoisture("Error")
                    updateLastWatered("Error")
                    _connectionState.value = ConnectionState.OFFLINE
                }
            }
        }
    }

    private suspend fun connectToArduino() {
        withContext(Dispatchers.IO) {
            try {
                _connectionState.value = ConnectionState.CONNECTING

                // Check for manually configured IP first
                val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
                val manualIp = prefs.getString("arduino_ip", "")

                val arduinoIp = if (!manualIp.isNullOrBlank()) {
                    // Try manual IP first
                    if (testConnection(manualIp)) manualIp else null
                } else {
                    // Fall back to network scanning
                    val scanner = NetworkScanner(this@MainActivity)
                    scanner.findArduino()
                }

                if (arduinoIp != null) {
                    Log.d("MainActivity", "Found Arduino at $arduinoIp")
                    connection = ArduinoConnection(arduinoIp, 8080)
                    connection.open()

                    val response = requestMoistureUpdate()
                    if (response != null) {
                        _connectionState.value = ConnectionState.CONNECTED
                    } else {
                        _connectionState.value = ConnectionState.OFFLINE
                    }
                } else {
                    Log.e("MainActivity", "Could not find Arduino on network")
                    _connectionState.value = ConnectionState.OFFLINE
                }

            } catch (e: Exception) {
                Log.e("MainActivity", "Connection attempt failed: ${e.message}")
                _connectionState.value = ConnectionState.OFFLINE
            }
        }
    }

    private suspend fun testConnection(ip: String): Boolean {
        return try {
            withTimeout(1000) {
                val testConnection = ArduinoConnection(ip, 8080)
                testConnection.open()
                val response = testConnection.receiveJson()
                testConnection.close()
                response != null
            }
        } catch (e: Exception) {
            false
        }
    }

    // Get Arduino connection status
    fun getArduinoConnectionStatus(): Boolean {
        return connection.isConnected()
    }
}
