package com.project.sproutling.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.project.sproutling.screens.AddPlant
import com.project.sproutling.screens.HomeScreen
import com.project.sproutling.screens.NotificationScreen
import com.project.sproutling.screens.PlantsScreen
import com.project.sproutling.screens.ProfileScreen
import com.project.sproutling.screens.SettingScreen

@Composable
fun SetUpNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    onWaterPlant: () -> Unit,
    requestMoistureUpdate: suspend () -> String,
    parseMoistureResponse: (String) -> Double
) {
    NavHost(navController = navController,
        startDestination = Screens.Home.route){
            composable(Screens.Home.route){
                HomeScreen(innerPadding = innerPadding, navController = navController, onWaterPlant = { onWaterPlant() },
                    requestMoistureUpdate = requestMoistureUpdate, parseMoistureResponse = parseMoistureResponse)
            }
            composable(Screens.Notification.route){
                NotificationScreen(innerPadding = innerPadding)
            }
            composable(Screens.Profile.route){
                ProfileScreen(innerPadding = innerPadding)
            }

            composable(Screens.Setting.route){
                SettingScreen(innerPadding = innerPadding)
            }

            composable(Screens.Plants.route){
                PlantsScreen(innerPadding = innerPadding)
            }

            composable(Screens.AddPlant.route) {
                AddPlant(innerPadding = innerPadding, navController = navController)
            }
    }
}