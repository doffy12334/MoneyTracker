package com.example.moneytracker.data

import com.example.moneytracker.data.local.SharedPrefsManager
import com.example.moneytracker.domain.model.AppCurrency
import com.example.moneytracker.domain.model.AppLanguage
import com.example.moneytracker.domain.model.AppSettings
import com.example.moneytracker.domain.model.AppTheme
import com.example.moneytracker.domain.repository.SettingsRepository

class SharedPreferencesSettingsRepository(
    private val sharedPrefsManager: SharedPrefsManager
) : SettingsRepository {
    override fun getSettings(): AppSettings {
        return AppSettings(
            notificationsEnabled = sharedPrefsManager.isNotificationEnabled(),
            language = AppLanguage.fromCode(sharedPrefsManager.getLanguageCode()),
            theme = AppTheme.fromValue(sharedPrefsManager.getThemeValue()),
            currency = AppCurrency.fromCode(sharedPrefsManager.getCurrencyCode())
        )
    }

    override fun setNotificationsEnabled(enabled: Boolean) {
        sharedPrefsManager.setNotificationEnabled(enabled)
    }

    override fun setLanguage(language: AppLanguage) {
        sharedPrefsManager.setLanguageCode(language.code)
    }

    override fun setTheme(theme: AppTheme) {
        sharedPrefsManager.setThemeValue(theme.value)
    }

    override fun setCurrency(currency: AppCurrency) {
        sharedPrefsManager.setCurrencyCode(currency.code)
    }
}
