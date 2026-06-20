package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.R
import com.example.moneytracker.data.AuthException
import com.example.moneytracker.domain.usecase.SendPasswordResetEmailUseCase
import com.example.moneytracker.domain.usecase.SendPhoneOtpUseCase
import com.example.moneytracker.presentation.uistate.InputEmailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InputEmailViewModel(
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
    private val sendPhoneOtpUseCase: SendPhoneOtpUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(InputEmailUiState())
    val uiState: StateFlow<InputEmailUiState> = _uiState.asStateFlow()

    fun onInputChanged(value: String) {
        _uiState.update {
            it.copy(
                input = value,
                isCodeSent = false,
                isEmailSent = false,
                verificationId = null,
                errorMessage = null
            )
        }
    }

    fun sendResetCode(activity: Any) {
        val input = _uiState.value.input.trim()
        if (input.isBlank()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, isCodeSent = false, isEmailSent = false, errorMessage = null)
            }
            try {
                if (isEmail(input)) {
                    sendPasswordResetEmailUseCase(input)
                    _uiState.update { it.copy(isLoading = false, isEmailSent = true) }
                } else {
                    val phoneNumber = normalizePhoneNumber(input)
                    val verificationId = sendPhoneOtpUseCase(phoneNumber, activity)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isCodeSent = true,
                            verificationId = verificationId
                        )
                    }
                }
            } catch (exception: Exception) {
                if (exception is AuthException) {
                    _uiState.update { it.copy(isLoading = false, errorMessageResId = exception.messageResId, errorMessage = exception.message) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessageResId = null, errorMessage = exception.message ?: "Authentication failed") }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetNavigation() {
        _uiState.update { it.copy(isCodeSent = false, isEmailSent = false) }
    }

    private fun isEmail(input: String): Boolean = input.contains("@")

    private fun normalizePhoneNumber(input: String): String {
        return if (input.startsWith("+")) input else "+84${input.trimStart('0')}"
    }

    class Factory(
        private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
        private val sendPhoneOtpUseCase: SendPhoneOtpUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InputEmailViewModel::class.java)) {
                return InputEmailViewModel(
                    sendPasswordResetEmailUseCase,
                    sendPhoneOtpUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
