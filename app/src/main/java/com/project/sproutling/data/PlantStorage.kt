package com.project.sproutling.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PlantStorage(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("PlantStorage", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Save a list of plants
    private fun savePlants(plants: List<Plant>) {
        val jsonString = gson.toJson(plants)
        sharedPreferences.edit().putString("plants", jsonString).apply()
    }

    // Get all plants
    fun getPlants(): List<Plant> {
        val json = sharedPreferences.getString("plants", null)
        return if (json != null) {
            val type = object : TypeToken<List<Plant>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    // Add a single plant
    fun addPlant(plant: Plant) {
        val currentPlants = getPlants().toMutableList()
        currentPlants.add(plant)
        val json = gson.toJson(currentPlants)
        sharedPreferences.edit().putString("plants", json).apply()
    }

    // Remove a single plant by name
    fun removePlantByName(name: String) {
        val currentPlants = getPlants().toMutableList()
        currentPlants.removeAll { it.name == name }
        savePlants(currentPlants)
    }

    // Get the last watered time for a plant
    fun getLastWatered(plantName: String): String? {
        val plants = getPlants()
        return plants.find { it.name == plantName }?.lastWatered
    }

    fun getLastMoisture(): String? {
        val sharedPrefs = context.getSharedPreferences("plant_data", Context.MODE_PRIVATE)
        return sharedPrefs.getString("last_moisture", null)
    }

    fun setLastMoisture(moisture: String) {
        val sharedPrefs = context.getSharedPreferences("plant_data", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("last_moisture", moisture).apply()
    }

    // Update last watered time for a plant
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateLastWatered(plantName: String) {
        val plants = getPlants().toMutableList()
        val plant = plants.find { it.name == plantName }

        if (plant != null) {
            // If justWatered is true, set the current time as the last watered time
            plant.lastWatered = getCurrentFormattedTime() // Store as string of timestamp
            savePlants(plants) // Save the updated list back to SharedPreferences
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentFormattedTime(): String {
        // Get current time in milliseconds (instant)
        val currentTimeMillis = System.currentTimeMillis()

        // Create an Instant object from current time
        val instant = Instant.ofEpochMilli(currentTimeMillis)

        // Define the formatter for the desired output format
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())  // Use the system's default time zone

        // Format the instant to a string
        return formatter.format(instant)
    }
}