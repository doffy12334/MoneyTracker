package com.example.moneytracker.presentation.uistate

import com.example.moneytracker.domain.model.settings.AppLanguage
import com.example.moneytracker.domain.model.settings.AppCurrency
import com.example.moneytracker.domain.model.settings.AppTheme

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val language: AppLanguage = AppLanguage.VIETNAMESE,
    val theme: AppTheme = AppTheme.LIGHT,
    val currency: AppCurrency = AppCurrency.VND,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
