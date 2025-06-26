package fi.tuni.weather_app.navigation

import android.R.id.tabs
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import fi.tuni.weather_app.components.WeatherScreen
import fi.tuni.weather_app.screens.HomeScreen
import fi.tuni.weather_app.screens.MapPickerScreen
import fi.tuni.weather_app.screens.SettingsScreen
import fi.tuni.weather_app.viewmodel.SharedViewModel
import androidx.compose.runtime.getValue

@Composable
fun WeatherAppNavHost(viewModel: SharedViewModel) {
    val navController = rememberNavController()
    val sharedViewModel: SharedViewModel = viewModel

    // List of tabs available
    val tabs = listOf(Screen.Home, Screen.Maps, Screen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route
    val selectedTabIndex = tabs.indexOfFirst { it.route == currentRoute }.takeIf { it >= 0 } ?: 0

    Column {
        // Draw tabs to the top of the app
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, screen ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    text = { Text(screen.route.capitalize()) }
                )
            }
        }

        // Set up navigation between screens
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = navController, viewModel = sharedViewModel)
            }

            composable(Screen.Maps.route) {
                MapPickerScreen(navController = navController, viewModel = sharedViewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController, viewModel = sharedViewModel)
            }

            composable(
                route = Screen.WeatherDetail.route,
                arguments = listOf(navArgument("dayIndex") { type = NavType.IntType })
            ) { backStackEntry ->
                val dayIndex = backStackEntry.arguments?.getInt("dayIndex") ?: 0
                WeatherScreen(dayIndex = dayIndex, viewModel = sharedViewModel, navController = navController)
            }

        }
    }
}