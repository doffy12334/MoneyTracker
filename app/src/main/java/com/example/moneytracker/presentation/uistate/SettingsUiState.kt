package com.example.moneytracker.presentation.uistate

import com.example.moneytracker.domain.model.AppLanguage
import com.example.moneytracker.domain.model.AppTheme

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val language: AppLanguage = AppLanguage.VIETNAMESE,
    val theme: AppTheme = AppTheme.LIGHT,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
