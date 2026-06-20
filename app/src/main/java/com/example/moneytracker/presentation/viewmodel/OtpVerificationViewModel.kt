package com.example.moneytracker.presentation.viewmodel

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.R
import com.example.moneytracker.data.AuthException
import com.example.moneytracker.domain.usecase.LinkPhoneUseCase
import com.example.moneytracker.domain.usecase.SendPhoneOtpUseCase
import com.example.moneytracker.domain.usecase.VerifyPhoneOtpUseCase
import com.example.moneytracker.presentation.uistate.OtpVerificationUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OtpVerificationViewModel(
    private val verifyPhoneOtpUseCase: VerifyPhoneOtpUseCase,
    private val sendPhoneOtpUseCase: SendPhoneOtpUseCase,
    private val linkPhoneUseCase: LinkPhoneUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(OtpVerificationUiState())
    val uiState: StateFlow<OtpVerificationUiState> = _uiState.asStateFlow()

    private var countDownTimer: CountDownTimer? = null

    fun initialize(verificationId: String) {
        _uiState.update { it.copy(verificationId = verificationId) }
        startCountdown()
    }

    fun verifyOtp(code: String) {
        val verificationId = _uiState.value.verificationId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                verifyPhoneOtpUseCase(verificationId, code)
                _uiState.update { it.copy(isLoading = false, isVerified = true) }
            } catch (exception: Exception) {
                val errorRes = if (exception is AuthException) {
                    exception.messageResId
                } else {
                    R.string.error_invalid_otp
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = errorRes) }
            }
        }
    }

    fun linkPhone(code: String) {
        val verificationId = _uiState.value.verificationId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                linkPhoneUseCase(verificationId, code)
                _uiState.update { it.copy(isLoading = false, isPhoneLinked = true) }
            } catch (exception: Exception) {
                val errorRes = if (exception is AuthException) {
                    exception.messageResId
                } else {
                    R.string.error_verify_failed
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = errorRes) }
            }
        }
    }

    fun resendOtp(phoneNumber: String, activity: Any) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val newVerificationId = sendPhoneOtpUseCase(phoneNumber, activity)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        verificationId = newVerificationId
                    )
                }
                startCountdown()
            } catch (exception: Exception) {
                val errorRes = if (exception is AuthException) {
                    exception.messageResId
                } else {
                    R.string.error_send_code_failed
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = errorRes) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetNavigation() {
        _uiState.update { it.copy(isVerified = false, isPhoneLinked = false) }
    }

    private fun startCountdown() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(COUNTDOWN_MILLIS, TICK_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                _uiState.update {
                    it.copy(resendCountdown = (millisUntilFinished / TICK_INTERVAL).toInt())
                }
            }

            override fun onFinish() {
                _uiState.update { it.copy(resendCountdown = 0) }
            }
        }.start()
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }

    class Factory(
        private val verifyPhoneOtpUseCase: VerifyPhoneOtpUseCase,
        private val sendPhoneOtpUseCase: SendPhoneOtpUseCase,
        private val linkPhoneUseCase: LinkPhoneUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OtpVerificationViewModel::class.java)) {
                return OtpVerificationViewModel(
                    verifyPhoneOtpUseCase,
                    sendPhoneOtpUseCase,
                    linkPhoneUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    private companion object {
        const val COUNTDOWN_MILLIS = 60_000L
        const val TICK_INTERVAL = 1_000L
    }
}
