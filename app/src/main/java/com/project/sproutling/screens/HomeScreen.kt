package com.project.sproutling.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.project.sproutling.R
import com.project.sproutling.data.Plant
import com.project.sproutling.data.PlantStorage
import com.project.sproutling.utils.ConnectionState
import com.project.sproutling.utils.NotificationService
import com.project.sproutling.utils.SettingsStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    navController: NavHostController,
    onWaterPlant: () -> Unit,
    updateStatus: suspend (updateMoisture: (String) -> Unit, updateLastWatered: (String) -> Unit, plantStorage: PlantStorage) -> Unit,
    connectToArduino: suspend () -> Unit,
    connectionState: ConnectionState
) {
    Log.d("HomeScreen", "HomeScreen recomposition")
    val context = LocalContext.current
    val notificationService = remember { NotificationService(context) }
    val plantStorage = remember { PlantStorage(context) }
    val plants = remember { mutableStateOf(plantStorage.getPlants()) }
    val moisture = remember { mutableStateOf("Loading...") }
    val lastWatered = remember { mutableStateOf("Loading...") }
    val settingsStorage = remember { SettingsStorage(context) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(247, 247, 247))
    ) {
        // If there is a plant available, check its status and update the interface
        if (plants.value.isEmpty()) {
            NoPlant(innerPadding, navController)
        } else {
            LaunchedEffect(connectionState) {
                Log.d("HomeScreen", "LaunchedEffect started with state: $connectionState")

                when (connectionState) {
                    ConnectionState.CONNECTED -> {
                        try {
                            // Set up periodic updates and notifications
                            while (true) {
                                updateStatus(
                                    { newMoisture ->
                                        moisture.value = newMoisture
                                        // Check moisture level and send alert if needed
                                        notificationService.checkAndSendMoistureAlert(
                                            plants.value.first().name,
                                            newMoisture
                                        )
                                    },
                                    { newLastWatered ->
                                        lastWatered.value = newLastWatered
                                        // Check watering time and send reminder if needed
                                        notificationService.checkAndSendWateringReminder(
                                            plants.value.first().name,
                                            newLastWatered
                                        )
                                    },
                                    plantStorage
                                )
                                // Convert minutes to milliseconds
                                val intervalMs = settingsStorage.getCheckInterval() * 60 * 1000L
                                delay(intervalMs)
                            }
                        } catch (e: Exception) {
                            Log.e("HomeScreen", "Error in update", e)
                        }
                    }

                    ConnectionState.OFFLINE -> {
                        moisture.value = "Offline"
                        lastWatered.value = "Offline"
                    }

                    ConnectionState.CONNECTING -> {
                        moisture.value = "Loading..."
                        lastWatered.value = "Loading..."
                    }
                }
            }

            // Set up periodic plant messages
            LaunchedEffect(Unit) {
                while (true) {
                    delay(12 * 60 * 60 * 1000) // 12 hours
                    if (plants.value.isNotEmpty()) {
                        notificationService.sendPlantMessage(plants.value.first().name)
                    }
                }
            }
            PlantExists(
                innerPadding = innerPadding,
                navController = navController,
                plants = plants,
                onWaterPlant = onWaterPlant,
                moisture = moisture,
                lastWatered = lastWatered,
                updateStatus = updateStatus,
                connectToArduino = connectToArduino,
                connectionState = connectionState,
                notificationService = notificationService
            )
        }
    }
}

@Composable
fun NoPlant(innerPadding: PaddingValues, navController: NavHostController) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label
        Text(
            text = "No Plant to Show",
            color = Color(105, 137, 116),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
        // Plant Image
        Image(
            painter = painterResource(R.drawable.planticon),
            contentDescription = "No Plant Image",
            modifier = Modifier
                .size(250.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        ElevatedButton(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(width = 135.dp, height = 40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(105, 137, 116)),
            onClick = { navController.navigate("addPlant") })
        {
            Text(text = "Add a Plant", color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
fun PlantExists(
    innerPadding: PaddingValues,
    navController: NavHostController,
    plants: MutableState<List<Plant>>,
    onWaterPlant: () -> Unit,
    moisture: MutableState<String>,
    lastWatered: MutableState<String>,
    updateStatus: suspend (updateMoisture: (String) -> Unit, updateLastWatered: (String) -> Unit, plantStorage: PlantStorage) -> Unit,
    connectToArduino: suspend () -> Unit,
    connectionState: ConnectionState,
    notificationService: NotificationService
) {
    var showMenu by remember { mutableStateOf(false) }
    var showPopUp by remember { mutableStateOf(false) }
    val plantStorage = PlantStorage(LocalContext.current)
    val plant = plants.value.first()

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Name
        Text(
            text = plant.name,
            color = Color(105, 137, 116),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(bottom = 3.dp)
        )
        // Species
        Text(
            text = plant.species,
            color = Color(105, 137, 116),
            fontSize = 16.sp,
            fontStyle = FontStyle.Italic,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
        // Plant Image
        Surface(
            color = Color(195, 221, 227),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
        ) {
            Image(
                painter = painterResource(R.drawable.planticon2),
                contentDescription = "Plant Image",
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .padding(20.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Manage Plant menu button
            Box(
                modifier = Modifier
                    .zIndex(2f)
            ) {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .offset(x=(-5).dp)
                        .zIndex(3f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More",
                        tint = headerGreen
                    )
                }
                // Dropdown for menu
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier
                        .background(Color(225, 243, 242)),
                    offset = DpOffset((5).dp, (-5).dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Edit Plant",
                                fontSize = 15.sp,
                                color = Color(105, 137, 116),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )
                        },
                        onClick = {
                            showMenu = false
                            navController.navigate("editPlant")
                        }
                    )
                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(105, 137, 116).copy(alpha = 0.3f))
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Delete Plant",
                                color = Color.Red,
                                fontSize = 15.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )
                        },
                        onClick = {
                            showMenu = false
                            showPopUp = true
                        }
                    )
                }

                if (showPopUp) {
                    AlertDialog(
                        onDismissRequest = {
                            showPopUp = false
                        },
                        title = {
                            Text(text = "Deleting Plant")
                        },
                        text = {
                            Text(text = "Delete ${plant.name}? This action cannot be undone.")
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    // On confirmation, remove the plant from storage and hide the popup
                                    plantStorage.removePlantByName(plant.name)
                                    plants.value = plantStorage.getPlants()
                                    showPopUp = false
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showPopUp = false
                                }
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
        // First when statement for the blurb
        when (connectionState) {
            ConnectionState.CONNECTING -> ConnectingBlurb()
            else -> StatusBlurb(moisture = moisture.value, lastWatered = lastWatered.value)
        }

        // Second when statement for the buttons
        when (connectionState) {
            ConnectionState.CONNECTING -> {
                // Empty button-sized spacer
                Spacer(
                    modifier = Modifier
                        .padding(top = (18).dp)
                        .size(width = 135.dp, height = 39.dp) // Height accounts for both buttons + padding
                )
            }
            ConnectionState.OFFLINE -> {
                ElevatedButton(
                    modifier = Modifier
                        .padding(top = 18.dp)
                        .size(width = 175.dp, height = 40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(105, 137, 116)),
                    onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            connectToArduino()
                        }
                    }
                ) {
                    Text(text = "Retry Connection", color = Color.White, fontSize = 16.sp)
                }
            }
            ConnectionState.CONNECTED -> {
                //StatusBlurb(moisture = moisture.value, lastWatered = lastWatered.value)
                // Water and Refresh buttons
                ElevatedButton(
                    modifier = Modifier
                        .padding(top = 18.dp)
                        .size(width = 135.dp, height = 40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(121, 194, 211)),
                    onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            onWaterPlant()
                            delay(2500)
                            plantStorage.updateLastWatered(plant.name)
                            updateStatus(
                                { newMoisture -> moisture.value = newMoisture },
                                { newLastWatered -> lastWatered.value = newLastWatered },
                                plantStorage
                            )
                        }
                    }
                ) {
                    Text(text = "Water Me!", color = Color.White, fontSize = 16.sp)
                }
                ElevatedButton(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(width = 135.dp, height = 40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(105, 137, 116)),
                    onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            updateStatus(
                                { newMoisture -> moisture.value = newMoisture },
                                { newLastWatered -> lastWatered.value = newLastWatered },
                                plantStorage
                            )
                        }
                    }
                ) {
                    Text(text = "Refresh", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ConnectingBlurb() {
    Surface(
        color = Color(195, 221, 227),
        modifier = Modifier
            .padding(top = 18.dp)
            .clip(RoundedCornerShape(12.dp))
            .fillMaxWidth(0.85f)
            .height(100.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),  // Add same padding as StatusBlurb
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = Color(105, 137, 116),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Connecting...",
                color = Color(105, 137, 116),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun StatusBlurb(moisture: String, lastWatered: String) {
    Log.d("StatusBlurb", "Recomposing with moisture: $moisture, lastWatered: $lastWatered")


    // Calculate status by moisture
    val status: String = when (moisture) {
        "Loading..." -> "Loading..."
        "Error" -> "Error"
        "Offline" -> "Offline"
        else -> try {
            val moistureValue = moisture.toDouble()
            when (moistureValue) {
                in 75.0..100.0 -> "Healthy"
                in 50.0..74.9 -> "Needs Water Soon"
                in 0.0..49.9 -> "Dry"
                else -> "Invalid Moisture Level"
            }
        } catch (e: NumberFormatException) {
            "Error"
        }
    }

    val statusColor = when (status) {
        "Healthy" -> Color.Green
        "Needs Water Soon" -> Color.Yellow
        "Dry" -> Color.Red
        "Error" -> Color.Red
        "Offline" -> Color.Gray
        else -> Color.Black
    }

    Surface(
        color = Color(195, 221, 227),
        modifier = Modifier
            .padding(top = 18.dp)
            .clip(RoundedCornerShape(12.dp))
            .fillMaxWidth(0.85f)  // Changed from using fixed width
            .height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                // Categories
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxHeight()
                ) {
                    Text(
                        text = "Status: ",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Last Watered: ",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                // Values
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxHeight()
                ) {
                    Text(
                        text = status,
                        color = statusColor,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                    Text(
                        text = lastWatered,
                        color = Color.Black,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectionErrorDialog(
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Connection Error") },
        text = { Text("Could not connect to Arduino right now") },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}
