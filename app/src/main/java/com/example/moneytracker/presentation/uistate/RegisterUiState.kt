package com.example.moneytracker.presentation.uistate

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class OtpSent(
        val verificationId: String,
        val phoneNumber: String
    ) : RegisterUiState()
    object Registered : RegisterUiState()
    data class Error(val message: String? = null, val messageResId: Int? = null) : RegisterUiState()
}
