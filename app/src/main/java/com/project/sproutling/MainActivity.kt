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
import com.project.sproutling.utils.RetryPolicy
import com.project.sproutling.utils.isNetworkAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : ComponentActivity() {
    private var connection: ArduinoConnection = ArduinoConnection("", 0) // Default initialization
    private val isTesting = false // Switch to false for actual Arduino connection
    private val retryPolicy = RetryPolicy()
    private val _isReconnecting = MutableStateFlow(false)
    private val isReconnecting: StateFlow<Boolean> = _isReconnecting.asStateFlow()
    private val _connectionState = MutableStateFlow(ConnectionState.CONNECTING)
    private val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // Function to handle reconnection attempts
    private suspend fun handleReconnection() {
        if (_isReconnecting.value) return

        _isReconnecting.value = true
        try {
            _connectionState.value = ConnectionState.CONNECTING
            connectToArduino()
        } finally {
            _isReconnecting.value = false
        }
    }

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
                    val response = connection.sendCommandAndGetResponse("""{"command":"water_plant"}""")
                    Log.d("MainActivity", "Response from Arduino: $response")

                    withContext(Dispatchers.Main) {
                        if (response != null) {
                            true
                        } else {
                            null
                        }
                    }
                } catch (e: Exception) {
                    Log.d("MainActivity", "Expected disconnect during watering: ${e.message}")
                    null
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

                    // Add a small delay before reading response
                    delay(100)

                    withTimeout(5000) { // 5 second timeout
                        val response = connection.receiveJson()
                        Log.d("MainActivity", "Received moisture response: $response")

                        // Don't close the connection after receiving
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
            try {
                val isConnected = connection.isConnected()

                if (!isConnected) {
                    handleReconnection()
                    return@withContext
                }

                val response = requestMoistureUpdate()

                withContext(Dispatchers.Main) {
                    if (response != null) {
                        val moistureValue = response["moisture"]
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
                    } else {
                        updateMoisture("Offline")
                        updateLastWatered("Offline")
                        _connectionState.value = ConnectionState.OFFLINE
                        handleReconnection()
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in updateStatus: ${e.message}")
                withContext(Dispatchers.Main) {
                    updateMoisture("Offline")
                    updateLastWatered("Offline")
                    _connectionState.value = ConnectionState.OFFLINE
                    handleReconnection()
                }
            }
        }
    }


    private suspend fun connectToArduino() {
        withContext(Dispatchers.IO) {
            try {
                _connectionState.value = ConnectionState.CONNECTING

                val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
                val arduinoIp = prefs.getString("arduino_ip", "")

                if (arduinoIp != null) {
                    Log.d("MainActivity", "Found Arduino at $arduinoIp")
                    connection = ArduinoConnection(arduinoIp, 8080)

                    var retryCount = 0
                    val maxRetries = 5
                    val retryDelay = 10_000L // 10 seconds

                    while (retryCount < maxRetries) {
                        try {
                            connection.open()

                            // Add delay after reconnection
                            delay(2000)

                            val response = requestMoistureUpdate()
                            if (response != null) {
                                _connectionState.value = ConnectionState.CONNECTED
                                return@withContext
                            } else {
                                _connectionState.value = ConnectionState.OFFLINE
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Connection attempt failed: ${e.message}")
                            _connectionState.value = ConnectionState.OFFLINE
                        }

                        retryCount++
                        delay(retryDelay)
                    }

                    // If we reach this point, all retries have failed
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
