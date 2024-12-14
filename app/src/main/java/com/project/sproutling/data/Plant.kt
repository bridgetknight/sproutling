package com.project.sproutling.data

data class Plant(
    val name: String,
    val species: String,
    var moisture: Double = 0.0,
    var lastWatered: String = "Not Watered Yet"
)