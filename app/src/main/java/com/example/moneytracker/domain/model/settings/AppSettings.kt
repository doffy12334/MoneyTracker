package com.example.moneytracker.domain.model.settings


data class AppSettings(
    val notificationsEnabled: Boolean,
    val language: AppLanguage,
    val theme: AppTheme,
    val currency: AppCurrency
)
