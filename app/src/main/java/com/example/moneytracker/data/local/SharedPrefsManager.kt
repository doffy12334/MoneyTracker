package com.example.moneytracker.data.local

import android.content.Context
import androidx.core.content.edit

class SharedPrefsManager(context: Context) {
    private val sharedPref = context.getSharedPreferences("money_tracker_prefs", Context.MODE_PRIVATE)

    fun setNotificationEnabled(enabled: Boolean) {
        sharedPref.edit { putBoolean(KEY_NOTIFICATION_ENABLED, enabled) }
    }

    fun isNotificationEnabled(): Boolean = sharedPref.getBoolean(KEY_NOTIFICATION_ENABLED, true)

    fun setLanguageCode(languageCode: String) {
        sharedPref.edit { putString(KEY_LANGUAGE_CODE, languageCode) }
    }

    fun getLanguageCode(): String = sharedPref.getString(KEY_LANGUAGE_CODE, "vi") ?: "vi"

    fun setThemeValue(themeValue: String) {
        sharedPref.edit { putString(KEY_THEME_VALUE, themeValue) }
    }

    fun getThemeValue(): String = sharedPref.getString(KEY_THEME_VALUE, "light") ?: "light"

    private companion object {
        const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        const val KEY_LANGUAGE_CODE = "language_code"
        const val KEY_THEME_VALUE = "theme_value"
    }
}
