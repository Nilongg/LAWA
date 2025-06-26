package fi.tuni.weather_app.model

// a Combined for less api calls
data class WeatherResponse(
    val latitude: Double?,
    val longitude: Double?,
    val current_weather: CurrentWeather?,
    val daily: DailyWeather?
)

data class CurrentWeather(
    val temperature: Double?,
    val windspeed: Double?,
    val weathercode: Int?
)

data class DailyWeather(
    val time: List<String>?,
    val temperature_2m_max: List<Double>?,
    val temperature_2m_min: List<Double>?,
    val windspeed_10m_max: List<Double>?,
    val relative_humidity_2m_mean: List<Int>?,
    val sunrise: List<String>?,
    val sunset: List<String>?,
    val weather_code: List<Int>?
)
