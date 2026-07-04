package com.example.moneytracker.presentation.security

data class SecurityCenterUiState(
    val highValueProtectionEnabled: Boolean = false,
    val isPasswordResetLoading: Boolean = false,
    val isPasswordFormVisible: Boolean = false,
    val passwordResetSent: Boolean = false,
    val shouldLogoutAfterPasswordReset: Boolean = false,
    val messageResId: Int? = null,
    val message: String? = null,
    val errorMessage: String? = null
)
