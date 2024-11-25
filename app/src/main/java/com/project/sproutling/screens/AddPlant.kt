package com.project.sproutling.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.project.sproutling.R
import com.project.sproutling.data.Plant
import com.project.sproutling.data.PlantStorage
import com.project.sproutling.navigation.Screens

val headerGreen = Color(105, 137, 116)

@Composable
fun AddPlant(innerPadding: PaddingValues, navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    val context = LocalContext.current
    val plantStorage = remember { PlantStorage(context) }
    var showPopUp by remember { mutableStateOf(false)}

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .fillMaxHeight()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Your Plant",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(105, 137, 116),
            modifier = Modifier
                .padding(25.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(0.896f)
                .height(100.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                color = Color(195, 221, 227),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painterResource(R.drawable.planticon2),
                    contentDescription = "Plant Image",
                    modifier = Modifier
                        .size(95.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .padding(10.dp)
                )
            }
            // Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Plant Name", color = headerGreen, fontSize = 16.sp) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = headerGreen,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .padding(bottom = 11.dp)
            )
        } // End name/icon row
        Row(
            modifier = Modifier
                .fillMaxWidth(0.896f)
                .height(100.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Species Input
            OutlinedTextField(
                value = species,
                onValueChange = { species = it },
                label = { Text("Species", color = headerGreen, fontSize = 16.sp) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = headerGreen,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .padding(bottom = 11.dp)
                    .fillMaxWidth()
            )
        }
        ElevatedButton(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(width = 135.dp, height = 40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(105, 137, 116)),
            onClick = {
                val newPlant = Plant(name = name, species = species)
                plantStorage.addPlant(newPlant)
                showPopUp = true
            })
        {
            Text(text = "Save Plant", color = Color.White, fontSize = 16.sp)
        }

        if (showPopUp) {
            AlertDialog(
                onDismissRequest = {
                    // Handle dismiss if necessary
                    showPopUp = false
                },
                title = {
                    Text(text = "Plant Added")
                },
                text = {
                    Text(text = "Your plant has been successfully added.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // On confirmation, hide the popup and navigate back to home
                            showPopUp = false
                            navController.navigate("home") {
                                popUpTo(Screens.Home.route) {
                                    inclusive = true
                                }
                            }
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}