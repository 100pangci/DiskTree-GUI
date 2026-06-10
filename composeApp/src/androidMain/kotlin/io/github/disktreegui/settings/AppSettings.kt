package io.github.disktreegui.settings

import android.content.Context
import android.content.SharedPreferences

private lateinit var prefs: SharedPreferences

fun initAppSettings(context: Context) {
    prefs = context.getSharedPreferences("disktreegui_prefs", Context.MODE_PRIVATE)
}

actual object AppSettings {
    actual fun getString(key: String, default: String): String =
        if (::prefs.isInitialized) prefs.getString(key, default) ?: default else default

    actual fun putString(key: String, value: String) {
        if (::prefs.isInitialized) prefs.edit().putString(key, value).apply()
    }
}
