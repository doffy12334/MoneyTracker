package com.example.moneytracker.presentation.uistate

data class OtpVerificationUiState(
    val isLoading: Boolean = false,
    val isVerified: Boolean = false,
    val isPhoneLinked: Boolean = false,
    val errorMessage: Int? = null,
    val resendCountdown: Int = 0,
    val verificationId: String? = null
)
