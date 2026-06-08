package com.example.moneytracker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.presentation.util.AppNotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MoneyTrackerApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
        applySavedLanguage()
        applySavedTheme()
        refreshExchangeRates()
        AppNotificationScheduler(this).scheduleAll()
    }

    private fun refreshExchangeRates() {
        applicationScope.launch {
            AppContainer.refreshExchangeRatesUseCase()
        }
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
