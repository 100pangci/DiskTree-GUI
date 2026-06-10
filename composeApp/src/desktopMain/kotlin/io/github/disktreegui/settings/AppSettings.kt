package io.github.disktreegui.settings

import java.util.prefs.Preferences

actual object AppSettings {
    private val prefs = Preferences.userRoot().node("io/github/disktreegui")

    actual fun getString(key: String, default: String): String = prefs.get(key, default)
    actual fun putString(key: String, value: String) = prefs.put(key, value)
}
