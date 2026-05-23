package com.example.moneytracker.presentation.uistate

data class SecurityCenterUiState(
    val twoFactorEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val highValueProtectionEnabled: Boolean = false,
    val passwordResetSent: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)
