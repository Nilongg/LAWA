package fi.tuni.weather_app.components

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import fi.tuni.weather_app.helpers.formatDateWithWeekDay
import fi.tuni.weather_app.model.DailyWeather
import fi.tuni.weather_app.navigation.Screen
import fi.tuni.weather_app.viewmodel.SharedViewModel
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.util.Locale

@Composable
fun WeatherScreen(dayIndex: Int, viewModel: SharedViewModel, navController: NavController) {
    val forecast by viewModel.weather.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        dayIndex?.let { i ->
            forecast?.daily?.let { daily ->
                val originalDate = daily.time?.getOrNull(i) ?: ""
                var date = "Unknown date"
                if(!originalDate.isEmpty()) {
                    date = formatDateWithWeekDay(originalDate)
                } else {
                    date = originalDate
                }
                val maxTemp = daily.temperature_2m_max?.getOrNull(i) ?: 0.0
                val minTemp = daily.temperature_2m_min?.getOrNull(i) ?: 0.0
                val windSpeed = daily.windspeed_10m_max?.getOrNull(i) ?: 0.0
                val humidity = daily.relative_humidity_2m_mean?.getOrNull(i) ?: 0
                val sunrise = daily.sunrise?.getOrNull(i)?.split("T")[1] ?: "00:00"
                val sunset = daily.sunset?.getOrNull(i)?.split("T")[1] ?: "00:00"
                val _weathercode = daily.weather_code?.getOrNull(i) ?: 0
                val weathercode = viewModel.convertWeatherCode(_weathercode)
                Log.d("Weather app", date)


                Text("$date")
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (viewModel.fahrenheitOn.value) {
                        "High: ${maxTemp} °F"
                    } else {
                        "High: ${maxTemp} °C"
                    },
                )
                Text(
                    text = if (viewModel.fahrenheitOn.value) {
                        "Low: ${minTemp} °F"
                    } else {
                        "Low: ${minTemp} °C"
                    },
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text("Windspeed: $windSpeed km/h")
                Text("Humidity: $humidity%")
                Spacer(modifier = Modifier.height(24.dp))
                Text("sunrise at $sunrise")
                Text("sunset at $sunset")
                Spacer(modifier = Modifier.height(24.dp))
                Text("Average weather: $weathercode")

                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { navController.navigate(Screen.Home.route) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Go back")
                }

            }
        } ?: Text("No data available")
    }
}


