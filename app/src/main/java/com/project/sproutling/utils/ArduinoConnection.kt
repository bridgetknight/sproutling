package com.project.sproutling.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Job
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket

open class ArduinoConnection(private val ipAddress: String, private val portNumber: Int) {
    private var socket: Socket? = null
    private var job: Job? = null

    @Synchronized
    fun open() {
        try {
            close() // Close existing connection if any
            socket = Socket()
            val socketAddress = InetSocketAddress(ipAddress, portNumber)
            socket?.connect(socketAddress, 10000)  // 10-second timeout

            if (socket?.isConnected == true) {
                Log.d("ArduinoConnection", "Successfully connected to $ipAddress:$portNumber")
            } else {
                Log.e("ArduinoConnection", "Connection failed")
                throw IOException("Failed to connect")
            }
        } catch (e: Exception) {
            Log.e("ArduinoConnection", "Connection error: ${e.message}")
            socket = null
            throw e
        }
    }

    @Synchronized
    fun close() {
        try {
            job?.cancel()
            socket?.close()
        } catch (e: Exception) {
            Log.e("ArduinoConnection", "Error closing socket: ${e.message}")
        } finally {
            socket = null
        }
    }

    fun isConnected(): Boolean {
        return try {
            socket?.isConnected == true && socket?.isClosed == false
        } catch (e: Exception) {
            false
        }
    }

    @Synchronized
    open fun sendJson(data: String) {
        try {
            if (!isConnected()) {
                Log.d("ArduinoConnection", "Socket disconnected, attempting to reconnect...")
                open()
            }

            Log.d("ArduinoConnection", "Sending data: $data")
            socket?.getOutputStream()?.write((data + "\n").toByteArray())
            socket?.getOutputStream()?.flush()
            Log.d("ArduinoConnection", "Data sent successfully")
        } catch (e: Exception) {
            Log.e("ArduinoConnection", "Error sending data: ${e.message}")
            close() // Close the broken connection
            throw e
        }
    }

    @Synchronized
    fun receiveJson(): Map<String, String>? {
        try {
            if (!isConnected()) {
                Log.d("ArduinoConnection", "Socket disconnected, attempting to reconnect...")
                open()
            }

            val input = socket?.getInputStream()
            if (input == null) {
                Log.e("ArduinoConnection", "Input stream is null")
                return null
            }

            val reader = BufferedReader(InputStreamReader(input))
            Log.d("ArduinoConnection", "Waiting for response...")

            val jsonResponse = reader.readLine()
            if (jsonResponse == null) {
                Log.e("ArduinoConnection", "Received null response")
                close()
                return null
            }

            Log.d("ArduinoConnection", "Received response: $jsonResponse")

            val jsonObject = JSONObject(jsonResponse)
            Log.d("ArduinoConnection", "Parsed JSON: $jsonObject")

            val responseMap = mutableMapOf<String, String>()
            jsonObject.keys().forEach { key ->
                responseMap[key] = jsonObject.get(key).toString()
            }

            return responseMap
        } catch (e: Exception) {
            Log.e("ArduinoConnection", "Error receiving/parsing data: ${e.message}")
            close() // Close the broken connection
            throw e
        }
    }
}

@RequiresApi(Build.VERSION_CODES.M)
fun isNetworkAvailable(context: Context) =
    (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
        getNetworkCapabilities(activeNetwork)?.run {
            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } ?: false
    }

@RequiresApi(Build.VERSION_CODES.M)
fun main() {
    // Test the connection to the Arduino socket
    val connection = ArduinoConnection("192.168.100.234", 80)
    connection.open()
}