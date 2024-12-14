package com.project.sproutling.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.project.sproutling.utils.SettingsStorage

@Composable
fun SettingScreen(
    innerPadding: PaddingValues,
    navController: NavHostController
) {
    val headerGreen = Color(105, 137, 116)
    val surfaceBlue = Color(195, 221, 227)

    val context = LocalContext.current
    val settingsStorage = remember { SettingsStorage(context) }

    // Notification preferences state
    var reminderNotifications by remember {
        mutableStateOf(settingsStorage.isNotificationEnabled("reminders"))
    }
    var messageNotifications by remember {
        mutableStateOf(settingsStorage.isNotificationEnabled("messages"))
    }
    var waterLevelNotifications by remember {
        mutableStateOf(settingsStorage.isNotificationEnabled("water_alerts"))
    }

    var arduinoIp by remember {
        mutableStateOf(settingsStorage.getArduinoIp())
    }

    var checkInterval by remember {
        mutableStateOf(settingsStorage.getCheckInterval())
    }

    var showIntervalDropdown by remember { mutableStateOf(false) }

    val intervals = listOf(
        "30 minutes" to 30,
        "1 hour" to 60,
        "2 hours" to 120,
        "4 hours" to 240,
        "8 hours" to 480,
        "12 hours" to 720,
        "24 hours" to 1440
    )

    // Update the switch onChange handlers
    Switch(
        checked = reminderNotifications,
        onCheckedChange = {
            reminderNotifications = it
            settingsStorage.setNotificationEnabled("reminders", it)
        }
    )

    Switch(
        checked = messageNotifications,
        onCheckedChange = {
            messageNotifications = it
            settingsStorage.setNotificationEnabled("messages", it)
        }
    )

    Switch(
        checked = waterLevelNotifications,
        onCheckedChange = {
            waterLevelNotifications = it
            settingsStorage.setNotificationEnabled("water_alerts", it)
        }
    )

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Settings",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = headerGreen,
            modifier = Modifier.padding(25.dp)
        )

        // Arduino Connection Section
        Surface(
            color = surfaceBlue,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Arduino Connection",
                    color = headerGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = arduinoIp,
                    onValueChange = {
                        arduinoIp = it
                        settingsStorage.setArduinoIp(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = headerGreen,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )

                Text(
                    text = "Enter your Arduino's IP address (e.g., 192.168.171.234)",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        // Notifications Section
        Surface(
            color = surfaceBlue,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = "Notifications",
                    color = headerGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Reminders Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Watering Reminders",
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                    Switch(
                        checked = reminderNotifications,
                        onCheckedChange = { reminderNotifications = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = headerGreen,
                            checkedTrackColor = headerGreen.copy(alpha = 0.5f)
                        )
                    )
                }

                // Plant Messages Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Plant Messages",
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                    Switch(
                        checked = messageNotifications,
                        onCheckedChange = { messageNotifications = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = headerGreen,
                            checkedTrackColor = headerGreen.copy(alpha = 0.5f)
                        )
                    )
                }

                // Low Water Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Low Moisture Alerts",
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                    Switch(
                        checked = waterLevelNotifications,
                        onCheckedChange = { waterLevelNotifications = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = headerGreen,
                            checkedTrackColor = headerGreen.copy(alpha = 0.5f)
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Check Interval",
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                    Box {
                        OutlinedButton(
                            onClick = { showIntervalDropdown = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = headerGreen
                            ),
                            border = BorderStroke(1.dp, headerGreen)
                        ) {
                            Text(
                                text = intervals.find { it.second == checkInterval }?.first ?: "1 hour",
                                color = headerGreen
                            )
                        }

                        DropdownMenu(
                            expanded = showIntervalDropdown,
                            onDismissRequest = { showIntervalDropdown = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            intervals.forEach { (label, minutes) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        checkInterval = minutes
                                        settingsStorage.setCheckInterval(minutes)
                                        showIntervalDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "How often to check moisture levels",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
    }
}