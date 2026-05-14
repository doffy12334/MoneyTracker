package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.domain.usecase.SendPasswordResetEmailUseCase
import com.example.moneytracker.domain.usecase.VerifyPasswordResetCodeUseCase
import com.example.moneytracker.presentation.uistate.ForgotPasswordUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
    private val verifyPasswordResetCodeUseCase: VerifyPasswordResetCodeUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onResetCodeChanged(code: String) {
        _uiState.update { it.copy(resetCode = code, errorMessage = null) }
    }

    fun sendResetEmail() {
        val email = _uiState.value.email
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                sendPasswordResetEmailUseCase(email)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isEmailSent = true,
                        canResend = false,
                        remainingSeconds = ForgotPasswordUiState.RESEND_SECONDS
                    )
                }
                startCountdown()
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Khong the gui email khoi phuc mat khau"
                    )
                }
            }
        }
    }

    fun resendEmail() {
        if (_uiState.value.canResend) {
            sendResetEmail()
        }
    }

    fun verifyResetCode() {
        val code = _uiState.value.resetCode
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val email = verifyPasswordResetCodeUseCase(code)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isCodeVerified = true,
                        verifiedEmail = email
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Ma xac thuc khong dung hoac da het han"
                    )
                }
            }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (seconds in ForgotPasswordUiState.RESEND_SECONDS downTo 1) {
                _uiState.update { it.copy(remainingSeconds = seconds, canResend = false) }
                delay(ONE_SECOND_MILLIS)
            }
            _uiState.update { it.copy(remainingSeconds = 0, canResend = true) }
        }
    }

    class Factory(
        private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
        private val verifyPasswordResetCodeUseCase: VerifyPasswordResetCodeUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
                return ForgotPasswordViewModel(
                    sendPasswordResetEmailUseCase,
                    verifyPasswordResetCodeUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    private companion object {
        const val ONE_SECOND_MILLIS = 1_000L
    }
}
