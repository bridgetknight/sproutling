package com.project.sproutling.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.project.sproutling.data.PlantStorage
import com.project.sproutling.screens.AddPlant
import com.project.sproutling.screens.EditPlant
import com.project.sproutling.screens.HomeScreen
import com.project.sproutling.screens.NotificationScreen
import com.project.sproutling.screens.PlantsScreen
import com.project.sproutling.screens.ProfileScreen
import com.project.sproutling.screens.SettingScreen
import com.project.sproutling.utils.ConnectionState

@Composable
fun SetUpNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    onWaterPlant: () -> Unit,
    updateStatus: suspend (updateMoisture: (String) -> Unit, updateLastWatered: (String) -> Unit, plantStorage: PlantStorage) -> Unit,
    connectToArduino: suspend () -> Unit,
    connectionState: ConnectionState
) {
    NavHost(navController = navController,
        startDestination = Screens.Home.route){
            composable(Screens.Home.route){
                HomeScreen(
                    innerPadding = innerPadding, navController = navController, onWaterPlant = { onWaterPlant() }, updateStatus = updateStatus, connectToArduino = connectToArduino, connectionState = connectionState)
            }
            composable(Screens.Notification.route){
                NotificationScreen(innerPadding = innerPadding)
            }
            composable(Screens.Profile.route){
                ProfileScreen(innerPadding = innerPadding)
            }

            composable(Screens.Setting.route){
                SettingScreen(innerPadding = innerPadding, navController = navController)
            }

            composable(Screens.Plants.route){
                PlantsScreen(innerPadding = innerPadding)
            }

            composable(Screens.AddPlant.route) {
                AddPlant(innerPadding = innerPadding, navController = navController)
            }

            composable(Screens.EditPlant.route) {
                EditPlant(innerPadding = innerPadding, navController = navController)
            }

    }
}