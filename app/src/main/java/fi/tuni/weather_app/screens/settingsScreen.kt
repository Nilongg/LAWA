package fi.tuni.weather_app.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fi.tuni.weather_app.navigation.Screen
import fi.tuni.weather_app.ui.theme.WeatherAppTheme
import fi.tuni.weather_app.viewmodel.SharedViewModel

@Composable
fun SettingsScreen(navController: NavController, viewModel: SharedViewModel, context: Context = LocalContext.current) {
    var showDialog by remember { mutableStateOf(false) }
    val darkModeOn by viewModel.darkmodeOn.collectAsState()
    val fahrenheitOn by viewModel.fahrenheitOn.collectAsState()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Clear Location History?") },
            text = { Text("Are you sure you want to clear the location history? \n \n" +
                    "The history will be cleared when the app is rebooted") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetLocHistory()
                    showDialog = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top

    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                Log.d("Weather app", "Test click from settings")
                viewModel.toggleDarkmode()
            }
        ) {
            if (darkModeOn) {
                Text("Turn on light mode")
            } else {
                Text("Turn on dark mode")
            }

        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.toggleFahrenheit()
            }
        ) {
            if(fahrenheitOn) {
                Text("Use fahrenheit")
            } else {
                Text("Use celsius")
            }

        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                showDialog = true
            }
        ) {
            Text("Clear the location history")
        }
    }
}