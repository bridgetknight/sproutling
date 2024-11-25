package com.project.sproutling.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket

open class ArduinoConnection(private val ipAddress: String, private val portNumber: Int) {
    private var socket: Socket? = null
    private var job: Job? = null
    fun open() {
        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                socket = Socket(ipAddress, portNumber)
                if (socket?.isConnected == true) {
                    println("Connected to Arduino")
                    Log.d("ArduinoConnection", "Connected to Arduino")
                } else {
                    println("Failed to connect to Arduino")
                    Log.d("ArduinoConnection", "Failed to connect to Arduino")
                }
            } catch (e: IOException) {
                Log.e("ArduinoConnection", "Failed to connect: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }

    fun close() {
        job?.cancel()
        socket?.close()
        socket = null
    }

    fun isConnected(): Boolean {
        return socket?.isConnected == true
    }

    open fun sendJson(data: String) {
        socket?.getOutputStream()?.write((data + "\n").toByteArray())
    }

    open fun receiveJson(): String {
        val input = socket?.getInputStream()
        val reader = BufferedReader(InputStreamReader(input))
        return reader.readLine()
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
    val connection = ArduinoConnection("192.168.181.234", 80)
    connection.open()
}