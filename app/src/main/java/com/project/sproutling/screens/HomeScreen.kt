package com.project.sproutling.screens

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.project.sproutling.R
import com.project.sproutling.data.Plant
import com.project.sproutling.data.PlantStorage

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    navController: NavHostController,
    onWaterPlant: () -> Unit
) {
    val context = LocalContext.current
    val plantStorage = remember { PlantStorage(context) }
    val plants = remember { mutableStateOf(plantStorage.getPlants()) }

    Surface(modifier = Modifier
        .fillMaxSize()
        .background(Color(247, 247, 247))
    ) {
        /* Populate homepage based on if plant exists */
        if (plants.value.isNotEmpty()) {
            PlantExists(innerPadding, navController, plants, onWaterPlant)
        } else {
            NoPlant(innerPadding, navController)
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
    onWaterPlant: () -> Unit
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
                    modifier = Modifier,
                    offset = DpOffset((5).dp, (-5).dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Delete Plant",
                                color = Color.Red,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
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
        // Plant Info
        Surface(
            color = Color(195, 221, 227),
            modifier = Modifier
                .padding(top = 18.dp)
                .clip(RoundedCornerShape(12.dp))
                .width(400.dp)
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
                            text = "Healthy",
                            color = Color.Green,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.End)
                        )
                        Text(
                            text = "02/04/2024 11:00 AM",
                            color = Color.Black,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
        ElevatedButton(
            modifier = Modifier
                .padding(top = 18.dp)
                .size(width = 135.dp, height = 40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(121, 194, 211)),
            onClick = { onWaterPlant() })
        {
            Text(text = "Water Me!", color = Color.White, fontSize = 16.sp)
        }
    }
}

