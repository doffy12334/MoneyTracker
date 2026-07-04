package com.example.moneytracker.presentation.auth

data class InputEmailUiState(
    val input: String = "",
    val isLoading: Boolean = false,
    val isCodeSent: Boolean = false,
    val isEmailSent: Boolean = false,
    val verificationId: String? = null,
    val errorMessageResId: Int? = null,
    val errorMessage: String? = null
)
