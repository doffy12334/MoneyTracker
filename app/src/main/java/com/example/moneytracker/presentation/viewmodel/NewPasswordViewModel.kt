package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.R
import com.example.moneytracker.data.AuthException
import com.example.moneytracker.domain.usecase.LoginUseCase
import com.example.moneytracker.domain.usecase.ResetPasswordWithPhoneUseCase
import com.example.moneytracker.presentation.uistate.NewPasswordUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewPasswordViewModel(
    private val resetPasswordWithPhoneUseCase: ResetPasswordWithPhoneUseCase,
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewPasswordUiState())
    val uiState: StateFlow<NewPasswordUiState> = _uiState.asStateFlow()

    fun resetPassword(
        email: String,
        newPassword: String,
        confirmPassword: String,
        isFromSecurityCenter: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Step 1: Update password then logout
                resetPasswordWithPhoneUseCase(newPassword, confirmPassword)
                _uiState.update { it.copy(isResetSuccessful = true) }

                if (!isFromSecurityCenter) {
                    // Step 2: Auto-login with email + new password
                    loginUseCase(email, newPassword)
                    _uiState.update { it.copy(isLoading = false, isAutoLoginSuccessful = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, isPasswordUpdatedAndLoggedOut = true) }
                }
            } catch (exception: Exception) {
                val errorRes = when {
                    exception is AuthException -> exception.messageResId
                    exception is IllegalArgumentException -> {
                        when {
                            exception.message?.contains("6") == true ->
                                R.string.security_password_too_short
                            exception.message?.contains("khớp") == true ->
                                R.string.security_password_mismatch
                            else -> R.string.error_reset_password_failed
                        }
                    }
                    else -> R.string.error_reset_password_failed
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = errorRes) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetNavigation() {
        _uiState.update { it.copy(isAutoLoginSuccessful = false, isPasswordUpdatedAndLoggedOut = false) }
    }

    class Factory(
        private val resetPasswordWithPhoneUseCase: ResetPasswordWithPhoneUseCase,
        private val loginUseCase: LoginUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NewPasswordViewModel::class.java)) {
                return NewPasswordViewModel(resetPasswordWithPhoneUseCase, loginUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
