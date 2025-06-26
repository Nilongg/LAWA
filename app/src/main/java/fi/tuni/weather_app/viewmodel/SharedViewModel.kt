package fi.tuni.weather_app.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import fi.tuni.weather_app.data.api.getApiKeyFromManifest
import fi.tuni.weather_app.data.repository.WeatherRepo
import fi.tuni.weather_app.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.round

// Testing purposes
// Will be heavily modified in the future

class SharedViewModel(application: Application) : AndroidViewModel(application) {
    // Allows access to fetching function*s
    private val repository = WeatherRepo(application)

    // Get the app context for the data store
    private val context = getApplication<Application>().applicationContext
    // Get api key for getting city name form lat and lng
    val apiKey: String? = getApiKeyFromManifest(context)

    // Data store for saving recent searches
    val Context.dataStore by preferencesDataStore(name = "searches")

    // This is where the locations are saved
    private val _locationHistoryState = MutableStateFlow<List<Pair<String, LatLng>>>(emptyList())
    val locationHistoryState: StateFlow<List<Pair<String, LatLng>>> = _locationHistoryState

    // For main screen

    // Used to determine the preferred temp unit
    private val _fahrenheitOn = MutableStateFlow<Boolean>(false)
    val fahrenheitOn = _fahrenheitOn

    private val _celsiusWeather = MutableStateFlow<WeatherResponse?>(null)
    private val _fahrenheitWeather = MutableStateFlow<WeatherResponse?>(null)

    // Contains the weather data (has fahrenheit and celsius temps)
    val weather: StateFlow<WeatherResponse?> = combine(
        _celsiusWeather, _fahrenheitWeather, fahrenheitOn
    ) { celsius, fahrenheit, useFahrenheit ->
        if (useFahrenheit) fahrenheit else celsius
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Contains the city name
    private val _city = MutableStateFlow("Loading...")
    val city: StateFlow<String> = _city

    // Day index for selected day
    private val _selectedDayIndex = MutableStateFlow<Int?>(null)
    val selectedDayIndex: StateFlow<Int?> = _selectedDayIndex

    // For Maps screen

    // The current selected location from maps
    private val _selectedMapsLoc = MutableStateFlow<LatLng?>(null)
    val selectedMapsLoc = _selectedMapsLoc

    // Used for getting the current location
    private val _allowToLocate = MutableStateFlow<Boolean>(true)
    val allowToLocate = _allowToLocate

    // For settings screen

    // Used to determine the darkmode's current state
    private val _darkmodeOn = MutableStateFlow<Boolean>(false)
    val darkmodeOn = _darkmodeOn

    /**
     * Toggles fahrenheitOn value between true and false
     * (allows the use of fahrenheit or not
     */
    fun toggleFahrenheit() {
        _fahrenheitOn.value = !_fahrenheitOn.value
    }

    /**
     * Converts between fahrenheit and celsius
     *
     */


    /**
     * Toggles the darkModeOn boolean off and on
     */
    fun toggleDarkmode() {
        _darkmodeOn.value = !_darkmodeOn.value
    }

    /**
     * Sets the forecast day index
     * amount of days = amount of indexes starting from 0
     *
     * @param index the day index
     */
    fun selectDay(index: Int) {
        _selectedDayIndex.value = index
    }

    /**
     * Sets the selected location in which will be displayed in the ui
     * and used for fetching the weather
     *
     * @param latLng the latitude and longitude as one
     */
    fun setSelectedLoc(latLng: LatLng?) {
        _selectedMapsLoc.value = latLng
    }

    /**
     * Sets the allowLocate between true and false
     * Used for locating the user
     *
     * @param value the boolean which determines if the user will be located
     */
    fun setAllowToLocate(value: Boolean) {
        _allowToLocate.value = value
    }

    /**
     * Fetches the current weather using latitude and longitude
     *
     * @param lat the latitude
     * @param lon the longitude
     */
    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val weather = repository.getWeather(lat, lon)
                _city.value = repository.getCityNameFromLocation(lat, lon, apiKey)
                // Rounds the celsius temps
                _celsiusWeather.value = weather.rounded()
                // Converts the temps to fahrenheit (also does rounding)
                _fahrenheitWeather.value = weather.convertedToFahrenheit()

            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error fetching current weather: ${e.localizedMessage}")
                _city.value = "Failed to load city"
            }
        }
    }

    /**
     * Saves the location to localStorage/cache
     *
     * @param loc the location to save
     */
    fun saveLocToCache(loc: LatLng?) {
        val key = stringPreferencesKey("searches")

        viewModelScope.launch(Dispatchers.IO)  {
            val city = repository.getCityNameFromLocation(loc?.latitude ?: 0.0,
                loc?.longitude ?: 0.0, apiKey)

            context.dataStore.edit { prefs ->
                prefs[key] = "${city};${loc?.latitude};${loc?.longitude}"
            }
        }
    }

    /**
     * Loads the location history from the data store
     *
     */
    fun loadLocHistory() {
        val key = stringPreferencesKey("searches")

        viewModelScope.launch(Dispatchers.IO) {
            context.dataStore.data
                .map { prefs -> prefs[key] }
                .collect { value ->
                    value?.split(";")?.let { parts ->
                        if (parts.size == 3) {
                            val city = parts[0]
                            val lat = parts[1].toDoubleOrNull()
                            val lng = parts[2].toDoubleOrNull()
                            val latLng = LatLng(lat ?: 0.0, lng ?: 0.0)

                            if (lat != null && lng != null) {
                                val newList = listOf(city to latLng) + _locationHistoryState.value
                                    .filterNot { it.first == city }
                                    .take(9) // Max entries
                                _locationHistoryState.value = newList
                            }
                        } else {
                            Log.d("Weather App", "no location data")
                        }
                    }
                }
        }
    }

    /**
     * Resets/clears the location history
     */
    fun resetLocHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            context.dataStore.edit { it.clear() }
        }
    }


    /**
     * Rounds the temperature
     * This is an extension function
     */
    fun WeatherResponse.rounded(): WeatherResponse = copy(
        current_weather = current_weather?.copy(
            temperature = current_weather.temperature?.let { round(it) }
        ),
        daily = daily?.copy(
            temperature_2m_max = daily.temperature_2m_max?.map { round(it) },
            temperature_2m_min = daily.temperature_2m_min?.map { round(it) }
        )
    )

    /**
     * Converts celsius to fahrenheit
     */
    fun WeatherResponse.convertedToFahrenheit(): WeatherResponse = copy(
        current_weather = current_weather?.copy(
            temperature = current_weather.temperature?.let { round(it * 9 / 5 + 32) }
        ),
        daily = daily?.copy(
            temperature_2m_max = daily.temperature_2m_max?.map { round(it * 9 / 5 + 32) },
            temperature_2m_min = daily.temperature_2m_min?.map { round(it * 9 / 5 + 32) }
        )
    )

    /**
     * Converts weather code to a string representation
     *
     * @param weatherCode the weather code as integer
     * @return weather code as a string representation
     */
    fun convertWeatherCode(weatherCode: Int?): String {
        return when (weatherCode) {
            0 -> "Clear sky"
            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            45 -> "Fog"
            48 -> "Rime fog"
            51 -> "Light Drizzle"
            53 -> "Drizzle"
            55 -> "Dense Drizzle"
            56 -> "Light Freezing Drizzle"
            57 -> "Dense Freezing Drizzle"
            61 -> "Light Rain"
            63 -> "Rain"
            65 -> "Heavy Rain"
            66 -> "Light Freezing Rain"
            67 -> "Heavy Freezing Rain"
            71 -> "Light Snowfall"
            73 -> "Snowfall"
            75 -> "Heavy Snowfall"
            77 -> "Snow grains"
            80 -> "Slight rain showers"
            81 -> "Moderate rain showers"
            82 -> "Heavy rain showers"
            85 -> "Slight snow showers"
            86 -> "Heavy snow showers"
            95 -> "Thunderstorm: Slight or moderate"
            96 -> "Light thunderstorm with hail"
            99 -> "Heavy thunderstorm with hail"
            else -> "Unknown weather condition"
        }
    }

}