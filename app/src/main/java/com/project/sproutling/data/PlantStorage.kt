package com.project.sproutling

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
        val jsonString = sharedPreferences.getString("plants", null)
        val type = object : TypeToken<List<Plant>>() {}.type
        return if (jsonString != null) gson.fromJson(jsonString, type) else emptyList()
    }

    // Add a single plant
    fun addPlant(plant: Plant) {
        val currentPlants = getPlants().toMutableList()
        currentPlants.add(plant)
        savePlants(currentPlants)
    }

    // Remove a single plant by name
    fun removePlantByName(name: String) {
        val currentPlants = getPlants().toMutableList()
        currentPlants.removeAll { it.name == name }
        savePlants(currentPlants)
    }
}