package com.project.sproutling.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlantStorage(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("PlantStorage", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Save a list of plants
    fun savePlants(plants: List<Plant>) {
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
}