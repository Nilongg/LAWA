package fi.tuni.weather_app

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.places.api.Places
import fi.tuni.weather_app.navigation.WeatherAppNavHost
import fi.tuni.weather_app.screens.HomeScreen
import fi.tuni.weather_app.ui.theme.WeatherAppTheme
import fi.tuni.weather_app.data.api.getApiKeyFromManifest
import fi.tuni.weather_app.viewmodel.SharedViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedViewModel: SharedViewModel by viewModels()

        val apiKey = getApiKeyFromManifest(this)
        Log.d("Weather App", apiKey.toString())

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        setContent {
            val isDarkModeOn by sharedViewModel.darkmodeOn.collectAsState()

            WeatherAppTheme(darkTheme = isDarkModeOn) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    WeatherAppNavHost(viewModel = sharedViewModel)
                }
            }
        }
    }
}
