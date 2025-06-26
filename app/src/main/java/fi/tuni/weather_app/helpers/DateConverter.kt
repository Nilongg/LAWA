package fi.tuni.weather_app.helpers

import android.os.Build
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Converts date to (weekday, date)
 */
fun formatDateWithWeekDay(dateAsString: String): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val date = LocalDate.parse(dateAsString, formatter)
    val day = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()).uppercase()
    val formattedDate = date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))

    return "$day, $formattedDate"

}