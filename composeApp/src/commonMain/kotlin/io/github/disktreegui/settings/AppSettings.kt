package io.github.disktreegui.settings

/**
 * 跨平台持久化存储接口，存储简单 key-value 字符串。
 * 各平台在 expect/actual 中实现。
 */
expect object AppSettings {
    fun getString(key: String, default: String): String
    fun putString(key: String, value: String)
}

object SettingsKeys {
    const val THEME_MODE = "theme_mode"
}
