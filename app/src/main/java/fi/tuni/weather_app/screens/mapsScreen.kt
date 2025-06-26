package fi.tuni.weather_app.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import fi.tuni.weather_app.navigation.Screen
import fi.tuni.weather_app.viewmodel.SharedViewModel

@Composable
fun MapPickerScreen(navController: NavController, viewModel: SharedViewModel, context: Context = LocalContext.current) {
    // The current selected location on the map
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }

    // Used for current the maps camera position
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }

    // For building the maps search function (autocomplete )
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            val latLng = place.latLng
            // Move map to this location
            latLng?.let {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 14f)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. Google Map in background
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                selectedLocation = latLng
            }
        ) {
            selectedLocation?.let {
                Marker(state = MarkerState(position = it))
            }
        }

        // 2. Overlay UI with buttons using Column
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter), // Align the whole column to the top center
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .build(context)
                    launcher.launch(intent)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Search Places")
            }
        }

        // 3. Confirm button at bottom, only if a location is selected
        selectedLocation?.let { location ->
            Button(
                onClick = {
                    Log.d("Weather App", location.toString())
                    viewModel.setSelectedLoc(location)
                    viewModel.saveLocToCache(location)
                    navController.navigate(Screen.Home.route)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text("Confirm Location")
            }
        }
    }
}
