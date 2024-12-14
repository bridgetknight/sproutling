package com.project.sproutling.utils

import android.content.Context

class SettingsStorage(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun setNotificationEnabled(type: String, enabled: Boolean) {
        sharedPreferences.edit().putBoolean(type, enabled).apply()
    }

    fun isNotificationEnabled(type: String): Boolean {
        return sharedPreferences.getBoolean(type, true) // Default to true
    }

    fun getArduinoIp(): String {
        return sharedPreferences.getString("arduino_ip", "") ?: ""
    }

    fun setArduinoIp(ip: String) {
        sharedPreferences.edit().putString("arduino_ip", ip).apply()
    }

    fun getCheckInterval(): Int {
        return sharedPreferences.getInt("check_interval", 60) // Default to 1 hour (60 minutes)
    }

    fun setCheckInterval(minutes: Int) {
        sharedPreferences.edit().putInt("check_interval", minutes).apply()
    }
}