package com.example.moneytracker.domain.model

data class AppSettings(
    val notificationsEnabled: Boolean,
    val language: AppLanguage,
    val theme: AppTheme,
    val currency: AppCurrency
)
