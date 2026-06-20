package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.domain.usecase.RegisterUseCase
import com.example.moneytracker.domain.usecase.SendPhoneOtpUseCase
import com.example.moneytracker.presentation.uistate.RegisterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase,
    private val sendPhoneOtpUseCase: SendPhoneOtpUseCase,
    private val authRepository: com.example.moneytracker.domain.repository.AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(
        email: String,
        password: String,
        confirmPassword: String,
        phoneNumber: String,
        activity: Any
    ) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            _uiState.value = try {
                // Step 1: Create account with email/password
                registerUseCase(email, password, confirmPassword)

                // Step 2: Send OTP to phone for verification
                val normalizedPhone = normalizePhoneNumber(phoneNumber)
                val verificationId = sendPhoneOtpUseCase(normalizedPhone, activity)

                // Navigate to OTP screen
                RegisterUiState.OtpSent(
                    verificationId = verificationId,
                    phoneNumber = normalizedPhone
                )
            } catch (exception: Exception) {
                // Rollback: if the account was created but phone OTP failed, delete the newly created account.
                try {
                    authRepository.deleteCurrentUser()
                } catch (e: Exception) {
                    // Ignore deletion errors
                }
                
                when (exception) {
                    is com.example.moneytracker.data.AuthException -> {
                        RegisterUiState.Error(messageResId = exception.messageResId, message = exception.message)
                    }
                    is IllegalArgumentException -> {
                        RegisterUiState.Error(message = exception.message)
                    }
                    else -> {
                        RegisterUiState.Error(message = "Đăng ký thất bại")
                    }
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }

    private fun normalizePhoneNumber(input: String): String {
        val trimmed = input.trim()
        return if (trimmed.startsWith("+")) trimmed else "+84${trimmed.trimStart('0')}"
    }

    class Factory(
        private val registerUseCase: RegisterUseCase,
        private val sendPhoneOtpUseCase: SendPhoneOtpUseCase,
        private val authRepository: com.example.moneytracker.domain.repository.AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                return RegisterViewModel(registerUseCase, sendPhoneOtpUseCase, authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
