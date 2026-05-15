package com.example.moneytracker.presentation.uistate

data class InputEmailUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isEmailSent: Boolean = false,
    val errorMessage: String? = null
)
