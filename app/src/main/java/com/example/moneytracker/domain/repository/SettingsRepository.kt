package com.example.moneytracker.domain.repository

import com.example.moneytracker.domain.model.AppCurrency
import com.example.moneytracker.domain.model.AppLanguage
import com.example.moneytracker.domain.model.AppSettings
import com.example.moneytracker.domain.model.AppTheme

interface SettingsRepository {
    fun getSettings(): AppSettings
    fun setNotificationsEnabled(enabled: Boolean)
    fun setLanguage(language: AppLanguage)
    fun setTheme(theme: AppTheme)
    fun setCurrency(currency: AppCurrency)
}
