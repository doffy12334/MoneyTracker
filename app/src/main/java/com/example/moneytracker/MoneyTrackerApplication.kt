package com.example.moneytracker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.moneytracker.di.AppContainer

class MoneyTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
        applySavedLanguage()
        applySavedTheme()
    }

    private fun applySavedLanguage() {
        val prefs = getSharedPreferences("money_tracker_prefs", MODE_PRIVATE)
        val languageCode = prefs.getString("language_code", "vi") ?: "vi"
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageCode)
        )
    }

    private fun applySavedTheme() {
        val prefs = getSharedPreferences("money_tracker_prefs", MODE_PRIVATE)
        AppCompatDelegate.setDefaultNightMode(
            when (prefs.getString("theme_value", "light")) {
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}
