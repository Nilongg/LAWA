package fi.tuni.weather_app.data.api
import fi.tuni.weather_app.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoService {
    @GET("forecast")
    suspend fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") current: Boolean = true,
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,windspeed_10m_max,relative_humidity_2m_mean,sunrise,sunset,weather_code",
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse
}
