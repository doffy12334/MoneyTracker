package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.R
import com.example.moneytracker.domain.usecase.GetProfileUseCase
import com.example.moneytracker.domain.usecase.GetSecuritySettingsUseCase
import com.example.moneytracker.domain.usecase.IsCurrentUserGoogleAccountUseCase
import com.example.moneytracker.domain.usecase.LogoutUseCase
import com.example.moneytracker.domain.usecase.SendPasswordResetEmailUseCase
import com.example.moneytracker.domain.usecase.SetBiometricEnabledUseCase
import com.example.moneytracker.domain.usecase.SetHighValueProtectionEnabledUseCase
import com.example.moneytracker.domain.usecase.UpdatePasswordUseCase
import com.example.moneytracker.presentation.uistate.SecurityCenterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SecurityCenterViewModel(
    private val getSecuritySettingsUseCase: GetSecuritySettingsUseCase,
    private val setBiometricEnabledUseCase: SetBiometricEnabledUseCase,
    private val setHighValueProtectionEnabledUseCase: SetHighValueProtectionEnabledUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
    private val isCurrentUserGoogleAccountUseCase: IsCurrentUserGoogleAccountUseCase,
    private val updatePasswordUseCase: UpdatePasswordUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SecurityCenterUiState())
    val uiState: StateFlow<SecurityCenterUiState> = _uiState.asStateFlow()

    init {
        loadSecuritySettings()
    }

    fun loadSecuritySettings() {
        val settings = getSecuritySettingsUseCase()
        _uiState.value = SecurityCenterUiState(
            biometricEnabled = settings.biometricEnabled,
            highValueProtectionEnabled = settings.highValueProtectionEnabled
        )
    }

    fun onBiometricChanged(enabled: Boolean) {
        setBiometricEnabledUseCase(enabled)
        _uiState.update {
            it.copy(
                biometricEnabled = enabled,
                messageResId = if (enabled) R.string.security_biometric_enabled else R.string.security_biometric_disabled,
                message = null,
                errorMessage = null
            )
        }
    }

    fun onHighValueProtectionChanged(enabled: Boolean) {
        setHighValueProtectionEnabledUseCase(enabled)
        _uiState.update {
            it.copy(
                highValueProtectionEnabled = enabled,
                messageResId = if (enabled) R.string.security_high_value_enabled else R.string.security_high_value_disabled,
                message = null,
                errorMessage = null
            )
        }
    }

    fun sendChangePasswordEmail() {
        if (_uiState.value.isPasswordResetLoading) return
        if (!isCurrentUserGoogleAccountUseCase()) {
            _uiState.update {
                it.copy(
                    isPasswordFormVisible = true,
                    messageResId = null,
                    message = null,
                    errorMessage = null
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPasswordResetLoading = true,
                    messageResId = null,
                    message = null,
                    errorMessage = null
                )
            }
            runCatching {
                val email = getProfileUseCase().email
                sendPasswordResetEmailUseCase(email)
                logoutUseCase()
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isPasswordResetLoading = false,
                        passwordResetSent = true,
                        shouldLogoutAfterPasswordReset = true,
                        messageResId = R.string.security_password_reset_sent,
                        message = null,
                        errorMessage = null
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isPasswordResetLoading = false,
                        errorMessage = exception.message ?: "Khong the gui email doi mat khau",
                        messageResId = null,
                        message = null
                    )
                }
            }
        }
    }

    fun updatePassword(newPassword: String, confirmPassword: String) {
        if (_uiState.value.isPasswordResetLoading) return
        val normalizedPassword = newPassword.trim()
        val normalizedConfirmPassword = confirmPassword.trim()
        if (normalizedPassword.length < MIN_PASSWORD_LENGTH) {
            _uiState.update { it.copy(messageResId = R.string.security_password_too_short, message = null, errorMessage = null) }
            return
        }
        if (normalizedPassword != normalizedConfirmPassword) {
            _uiState.update { it.copy(messageResId = R.string.security_password_mismatch, message = null, errorMessage = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPasswordResetLoading = true,
                    messageResId = null,
                    message = null,
                    errorMessage = null
                )
            }
            runCatching {
                updatePasswordUseCase(normalizedPassword)
                logoutUseCase()
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isPasswordResetLoading = false,
                        isPasswordFormVisible = false,
                        shouldLogoutAfterPasswordReset = true,
                        messageResId = R.string.security_password_update_success,
                        message = null,
                        errorMessage = null
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isPasswordResetLoading = false,
                        messageResId = null,
                        message = null,
                        errorMessage = exception.message ?: "Không thể đổi mật khẩu"
                    )
                }
            }
        }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(passwordResetSent = false, messageResId = null, message = null, errorMessage = null) }
    }

    fun consumeLogoutEvent() {
        _uiState.update { it.copy(shouldLogoutAfterPasswordReset = false) }
    }

    class Factory(
        private val getSecuritySettingsUseCase: GetSecuritySettingsUseCase,
        private val setBiometricEnabledUseCase: SetBiometricEnabledUseCase,
        private val setHighValueProtectionEnabledUseCase: SetHighValueProtectionEnabledUseCase,
        private val getProfileUseCase: GetProfileUseCase,
        private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
        private val isCurrentUserGoogleAccountUseCase: IsCurrentUserGoogleAccountUseCase,
        private val updatePasswordUseCase: UpdatePasswordUseCase,
        private val logoutUseCase: LogoutUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SecurityCenterViewModel::class.java)) {
                return SecurityCenterViewModel(
                    getSecuritySettingsUseCase,
                    setBiometricEnabledUseCase,
                    setHighValueProtectionEnabledUseCase,
                    getProfileUseCase,
                    sendPasswordResetEmailUseCase,
                    isCurrentUserGoogleAccountUseCase,
                    updatePasswordUseCase,
                    logoutUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
