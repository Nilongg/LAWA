package fi.tuni.weather_app.navigation

/**
 * This sealed class the is the navigation "cage"
 * Only these routes exist
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Maps : Screen("maps")
    object Settings : Screen("settings")
    object WeatherDetail : Screen("weather_detail/{dayIndex}") {
        fun createRoute(dayIndex: Int) = "weather_detail/$dayIndex"
    }
}
