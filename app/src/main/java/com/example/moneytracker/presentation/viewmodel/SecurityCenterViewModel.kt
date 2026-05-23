package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.domain.usecase.GetProfileUseCase
import com.example.moneytracker.domain.usecase.GetSecuritySettingsUseCase
import com.example.moneytracker.domain.usecase.SendPasswordResetEmailUseCase
import com.example.moneytracker.domain.usecase.SetBiometricEnabledUseCase
import com.example.moneytracker.domain.usecase.SetHighValueProtectionEnabledUseCase
import com.example.moneytracker.domain.usecase.SetTwoFactorEnabledUseCase
import com.example.moneytracker.presentation.uistate.SecurityCenterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SecurityCenterViewModel(
    private val getSecuritySettingsUseCase: GetSecuritySettingsUseCase,
    private val setTwoFactorEnabledUseCase: SetTwoFactorEnabledUseCase,
    private val setBiometricEnabledUseCase: SetBiometricEnabledUseCase,
    private val setHighValueProtectionEnabledUseCase: SetHighValueProtectionEnabledUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SecurityCenterUiState())
    val uiState: StateFlow<SecurityCenterUiState> = _uiState.asStateFlow()

    init {
        loadSecuritySettings()
    }

    fun loadSecuritySettings() {
        val settings = getSecuritySettingsUseCase()
        _uiState.value = SecurityCenterUiState(
            twoFactorEnabled = settings.twoFactorEnabled,
            biometricEnabled = settings.biometricEnabled,
            highValueProtectionEnabled = settings.highValueProtectionEnabled
        )
    }

    fun onTwoFactorChanged(enabled: Boolean) {
        setTwoFactorEnabledUseCase(enabled)
        _uiState.update {
            it.copy(
                twoFactorEnabled = enabled,
                message = if (enabled) "Da bat xac thuc 2 yeu to" else "Da tat xac thuc 2 yeu to",
                errorMessage = null
            )
        }
    }

    fun onBiometricChanged(enabled: Boolean) {
        setBiometricEnabledUseCase(enabled)
        _uiState.update {
            it.copy(
                biometricEnabled = enabled,
                message = if (enabled) "Da bat van tay / FaceID" else "Da tat van tay / FaceID",
                errorMessage = null
            )
        }
    }

    fun onHighValueProtectionChanged(enabled: Boolean) {
        setHighValueProtectionEnabledUseCase(enabled)
        _uiState.update {
            it.copy(
                highValueProtectionEnabled = enabled,
                message = if (enabled) "Da bat bao ve giao dich lon" else "Da tat bao ve giao dich lon",
                errorMessage = null
            )
        }
    }

    fun sendChangePasswordEmail() {
        viewModelScope.launch {
            runCatching {
                val email = getProfileUseCase().email
                sendPasswordResetEmailUseCase(email)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        passwordResetSent = true,
                        message = "Email doi mat khau da duoc gui",
                        errorMessage = null
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        errorMessage = exception.message ?: "Khong the gui email doi mat khau",
                        message = null
                    )
                }
            }
        }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(passwordResetSent = false, message = null, errorMessage = null) }
    }

    class Factory(
        private val getSecuritySettingsUseCase: GetSecuritySettingsUseCase,
        private val setTwoFactorEnabledUseCase: SetTwoFactorEnabledUseCase,
        private val setBiometricEnabledUseCase: SetBiometricEnabledUseCase,
        private val setHighValueProtectionEnabledUseCase: SetHighValueProtectionEnabledUseCase,
        private val getProfileUseCase: GetProfileUseCase,
        private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SecurityCenterViewModel::class.java)) {
                return SecurityCenterViewModel(
                    getSecuritySettingsUseCase,
                    setTwoFactorEnabledUseCase,
                    setBiometricEnabledUseCase,
                    setHighValueProtectionEnabledUseCase,
                    getProfileUseCase,
                    sendPasswordResetEmailUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
