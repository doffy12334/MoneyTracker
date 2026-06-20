package com.example.moneytracker.presentation.uistate

data class NewPasswordUiState(
    val isLoading: Boolean = false,
    val isResetSuccessful: Boolean = false,
    val isAutoLoginSuccessful: Boolean = false,
    val isPasswordUpdatedAndLoggedOut: Boolean = false,
    val errorMessage: Int? = null
)
