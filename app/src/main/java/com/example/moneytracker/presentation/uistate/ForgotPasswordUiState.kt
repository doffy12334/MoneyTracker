package com.example.moneytracker.presentation.uistate

data class ForgotPasswordUiState(
    val email: String = "",
    val resetCode: String = "",
    val remainingSeconds: Int = RESEND_SECONDS,
    val canResend: Boolean = true,
    val isLoading: Boolean = false,
    val isEmailSent: Boolean = false,
    val isCodeVerified: Boolean = false,
    val verifiedEmail: String? = null,
    val errorMessage: String? = null
) {
    companion object {
        const val RESEND_SECONDS = 60
    }
}
