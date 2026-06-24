package com.example.moneytracker.domain.repository

import com.example.moneytracker.domain.model.settings.AppCurrency
import com.example.moneytracker.domain.model.settings.AppLanguage
import com.example.moneytracker.domain.model.settings.AppSettings
import com.example.moneytracker.domain.model.settings.AppTheme

interface SettingsRepository {
    fun getSettings(): AppSettings
    fun setNotificationsEnabled(enabled: Boolean)
    fun setLanguage(language: AppLanguage)
    fun setTheme(theme: AppTheme)
    fun setCurrency(currency: AppCurrency)
}
