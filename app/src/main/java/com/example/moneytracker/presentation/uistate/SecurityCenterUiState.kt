package com.example.moneytracker.presentation.uistate

data class SecurityCenterUiState(
    val twoFactorEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val highValueProtectionEnabled: Boolean = false,
    val isPasswordResetLoading: Boolean = false,
    val isPasswordFormVisible: Boolean = false,
    val passwordResetSent: Boolean = false,
    val shouldLogoutAfterPasswordReset: Boolean = false,
    val messageResId: Int? = null,
    val message: String? = null,
    val errorMessage: String? = null
)
