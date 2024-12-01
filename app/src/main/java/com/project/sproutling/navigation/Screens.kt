package com.project.sproutling.navigation

sealed class Screens(var route: String) {
    object  Home : Screens("home")
    object  Profile : Screens("profile")
    object  Notification : Screens("notification")
    object  Setting : Screens("setting")
    object Plants : Screens("plants")
    object AddPlant : Screens("addPlant")
    object EditPlant : Screens("editPlant")

}