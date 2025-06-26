package fi.tuni.weather_app.data.repository

import android.content.Context
import android.util.Log
import fi.tuni.weather_app.data.api.RetrofitInstance
import fi.tuni.weather_app.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class WeatherRepo(private val context: Context) {
    private val api = RetrofitInstance.api

    suspend fun getWeather(lat: Double, lon: Double): WeatherResponse {
        return api.getWeather(lat, lon)
    }

    suspend fun getCityNameFromLocation(latitude: Double, longitude: Double, apiKey: String?): String =
        withContext(Dispatchers.IO) {
        try {
            // Base setup
            val url = URL(
                "https://maps.googleapis.com/maps/api/geocode/json?latlng=$latitude,$longitude&key=$apiKey"
            )
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            // If the response is ok, start parsing the "extracting" the city name from the json
            // If exception occurs, return "Failed to load city"
            // If the location doesn't exist in the google api then return "city not found"
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val results = json.getJSONArray("results")

                if (results.length() > 0) {
                    // Try to get locality from first result’s address components
                    val addressComponents = results.getJSONObject(0).getJSONArray("address_components")
                    val city = (0 until addressComponents.length())
                        .map { addressComponents.getJSONObject(it) }
                        .firstOrNull { component ->
                            val types = component.getJSONArray("types")
                            (0 until types.length()).any { types.getString(it) == "locality" }
                        }
                        ?.getString("long_name")

                    city ?: results.getJSONObject(0).getString("formatted_address") ?: "Unknown city"
                } else {
                    "City not found"
                }
            } else {
                "City not found"
            }
        } catch (e: Exception) {
            Log.e("Weather App", e.localizedMessage ?: "Unknown error")
            "Failed to load city..."
        }
    }
}
