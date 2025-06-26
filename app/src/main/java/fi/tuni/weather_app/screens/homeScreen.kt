package fi.tuni.weather_app.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.tabs.TabLayout
import fi.tuni.weather_app.helpers.formatDateWithWeekDay
import fi.tuni.weather_app.model.TempLocation
import fi.tuni.weather_app.navigation.Screen
import fi.tuni.weather_app.viewmodel.SharedViewModel

// For testing purposes
// Will be heavily modified in the future

@Composable
fun HomeScreen(navController: NavController, viewModel: SharedViewModel, context: Context = LocalContext.current) {
    // Get the location
    val locationText by viewModel.city.collectAsState()

    // collect weather from viewmodel
    val weather by viewModel.weather.collectAsState()

    // The location picked from the map
    val currentMapsLocation by viewModel.selectedMapsLoc.collectAsState()

    // Used when "locate me" is pressed and when the app is lauched for the first time
    // To locate the user
    val allowToLocate by viewModel.allowToLocate.collectAsState()

    // Used for dropdown
    var expanded by remember { mutableStateOf(false) }
    val savedLocations by viewModel.locationHistoryState.collectAsState()

    // Used for selecting locations from the dropdown
    var dropdownLocationChanged by remember { mutableStateOf(false) }

    var selectedOptionText by remember {
        if (savedLocations.isNotEmpty()) {
            mutableStateOf(savedLocations[0])
        } else {
            mutableStateOf(Pair("No locations", LatLng(0.0, 0.0)))
        }

    }

    // calls only once the component loads and when "allowToLocate" changes
    LaunchedEffect(allowToLocate, dropdownLocationChanged) {
        Log.d("Weather app", "Refresh Happened")

        viewModel.loadLocHistory()

        if ( allowToLocate ) {
            Log.d("Weather app", "This should only happen at the start")
            requestCurrentLocation(context) { location ->
                // Use Helsinki if location is null
                val helsinki = TempLocation(60.1695, 24.9354)
                val locToUse = TempLocation(location?.latitude ?: helsinki.latitude, location?.longitude ?: helsinki.longitude)

                viewModel.fetchWeather(locToUse.latitude, locToUse.longitude)
                viewModel.setAllowToLocate(false)

            }
        } else if (dropdownLocationChanged) {
            dropdownLocationChanged = false
        } else {
            Log.d("Weather app", "This should only happen when location is selected")
            // Check for if user has picked something from the map
            currentMapsLocation?.let {
                viewModel.fetchWeather(it.latitude, it.longitude)
                // Reset the selected maps location
                viewModel.setSelectedLoc(null)
            }
        }

    }
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Row {
            Icon(
                Icons.Filled.LocationOn,
                contentDescription = "Location Icon"
            )
            Box {
                Text(
                    text = locationText,
                    modifier = Modifier.clickable(
                        onClick = {
                            expanded = true
                        }
                    ),
                    fontSize = 24.sp
                )
                if (savedLocations.isNotEmpty()) {
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        savedLocations.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.first) },
                                onClick = {
                                    selectedOptionText = option
                                    expanded = false
                                    viewModel.setSelectedLoc(option.second)
                                    dropdownLocationChanged = true
                                }
                            )
                        }
                    }
                }

            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Check if weather is not null
        weather?.let {
            Text(
                text = if (viewModel.fahrenheitOn.value) {
                    "${it.current_weather?.temperature} °F"
                } else {
                    "${it.current_weather?.temperature} °C"
                },
                modifier = Modifier,
                fontSize = 26.sp
            )
            Text(
                text = viewModel.convertWeatherCode(it.current_weather?.weathercode),
                modifier = Modifier,
                fontSize = 26.sp
            )
        } ?: Text(
            text = "Loading...",
            modifier = Modifier,
            fontSize = 26.sp
        )

        Spacer(modifier = Modifier.height(64.dp))

        weather?.daily?.let { daily ->
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val days = daily.time
                items(days?.size ?: 0) { i ->
                    Text(
                        text = formatDateWithWeekDay(daily.time?.get(i).toString()) ?: "Error loading date",
                        modifier = Modifier.clickable {
                            Log.d("Weather app", "passing ${i} as a parameter")
                            viewModel.selectDay(i)
                            navController.navigate(Screen.WeatherDetail.createRoute(i))
                        }
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF05A0FA))
                            .padding(8.dp),

                        fontSize = 18.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center

                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } ?: Text("Loading forecast...")

        Spacer(modifier = Modifier.height(24.dp))

        // For getting the current location
        Button(
            onClick = {
                viewModel.setAllowToLocate(true)
            }
        ) {
            Text("Locate me")
        }
    }
}

/**
 * Requests the permissions to use location
 */
fun requestCurrentLocation(
    context: Context,
    onLocationResult: (Location?) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val locationRequest = CurrentLocationRequest.Builder()
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .build()

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.getCurrentLocation(locationRequest, null)
            .addOnSuccessListener { location ->
                onLocationResult(location)
            }
            .addOnFailureListener {
                onLocationResult(null)
            }
    } else {
        onLocationResult(null)
    }
}

