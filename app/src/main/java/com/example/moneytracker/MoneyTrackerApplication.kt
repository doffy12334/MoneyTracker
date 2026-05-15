package com.example.moneytracker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.moneytracker.di.AppContainer

class MoneyTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
        applySavedTheme()
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
