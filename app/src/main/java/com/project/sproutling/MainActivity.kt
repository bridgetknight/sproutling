package com.project.sproutling

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.project.sproutling.screens.MainScreen
import com.project.sproutling.ui.theme.SproutlingTheme
import com.project.sproutling.utils.ArduinoConnection
import com.project.sproutling.utils.MockArduinoConnection
import com.project.sproutling.utils.isNetworkAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : ComponentActivity() {
    private lateinit var connection: ArduinoConnection
    private val isTesting = true // Switch to false for actual Arduino connection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")

        connection = if (isTesting) MockArduinoConnection() else ArduinoConnection("192.168.181.234", 80)

        setContent {
            SproutlingTheme {
                MainScreen(onWaterPlant = {
                    waterPlant()
                })
            }
        }

        lifecycleScope.launch {
            val context: Context = this@MainActivity
            Log.d("MainActivity", "Checking network availability...")
            if (isNetworkAvailable(context)) {
                Log.d("MainActivity", "Network is available, attempting to connect to Arduino...")
                connectToArduino()
            } else {
                Log.d("MainActivity", "Network is not available")
            }
        }
    }

    private fun waterPlant() {
        lifecycleScope.launch {
            connection.sendJson("""{"command":"WATER"}""")
            val response = connection.receiveJson()
            Log.d("MainActivity", "Response from Arduino: $response")
        }
    }

    private suspend fun connectToArduino() {
        withContext(Dispatchers.IO) {
            //connection = ArduinoConnection("192.168.181.234", 80)
            connection.open()
            delay(500)
            if (connection.isConnected()) {
                Log.d("MainActivity", "Connected to Arduino")
                println("Connected to Arduino")
                // connection.close()
            } else {
                Log.e("MainActivity", "Failed to connect to Arduino")
                println("Failed to connect to Arduino")
            }
        }
    }
}
