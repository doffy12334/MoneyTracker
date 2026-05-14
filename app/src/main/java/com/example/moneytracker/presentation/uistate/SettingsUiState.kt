package com.example.moneytracker.presentation.uistate

data class SettingsUiState(
    val currencyCode: String = "USD",
    val isDarkMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
