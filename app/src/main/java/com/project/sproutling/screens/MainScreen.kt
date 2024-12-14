package com.project.sproutling.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.project.sproutling.data.PlantStorage
import com.project.sproutling.navigation.BottomNavigationBar
import com.project.sproutling.navigation.SetUpNavGraph
import com.project.sproutling.utils.ConnectionState
import com.project.sproutling.utils.bottomNavigationItemsList

/**
 * Created 28-02-2024 at 01:50 pm
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onWaterPlant: () -> Unit,
    requestMoistureUpdate: () -> Unit,
    updateStatus: suspend (updateMoisture: (String) -> Unit, updateLastWatered: (String) -> Unit, plantStorage: PlantStorage) -> Unit,
    connectToArduino: suspend () -> Unit,
    connectionState: ConnectionState
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute by remember(navBackStackEntry) {
        derivedStateOf {
            navBackStackEntry?.destination?.route
        }
    }
    /*val topBarTitle by remember(currentRoute) {
        derivedStateOf {
            if (currentRoute != null) {
                bottomNavigationItemsList[bottomNavigationItemsList.indexOfFirst {
                    it.route == currentRoute
                }].title
            } else {
                bottomNavigationItemsList[0].title
            }
        }
    }*/
    Scaffold(
        /*topBar = {
            TopAppBar(title = {
                // Text(text = topBarTitle)
                Text(
                    text = "Hello, User!"
                )
            },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
                )
        },*/
        bottomBar = {
            BottomNavigationBar(
                items = bottomNavigationItemsList,
                currentRoute = currentRoute
            ) { currentNavigationItem ->
                if (currentRoute != currentNavigationItem.route) {  // Only navigate if it's a different route
                    navController.navigate(currentNavigationItem.route) {
                        navController.graph.startDestinationRoute?.let { startDestinationRoute ->
                            // Pop up to the start destination, clearing the back stack
                            popUpTo(startDestinationRoute) {
                                inclusive = true // Ensure the stack is fully cleared
                                saveState = false // Disable saving state when clearing
                            }
                        }

                        // Configure navigation to avoid multiple instances of the same destination
                        launchSingleTop = true

                        // Restore state when re-selecting a previously selected item
                        restoreState = false
                    }
                }
            }
        }
    ) {innerPadding->
        SetUpNavGraph(navController = navController, innerPadding = innerPadding, onWaterPlant = { onWaterPlant() }, updateStatus = updateStatus, connectToArduino = { connectToArduino() }, connectionState = connectionState)
    }
}