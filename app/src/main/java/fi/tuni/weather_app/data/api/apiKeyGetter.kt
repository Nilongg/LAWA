package fi.tuni.weather_app.data.api

import android.content.Context
import android.content.pm.PackageManager

fun getApiKeyFromManifest(context: Context): String? {
    return try {
        val appInfo = context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        appInfo.metaData.getString("com.google.android.geo.API_KEY")
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    }
}