package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.domain.usecase.SendPasswordResetEmailUseCase
import com.example.moneytracker.presentation.uistate.InputEmailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InputEmailViewModel(
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(InputEmailUiState())
    val uiState: StateFlow<InputEmailUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                isEmailSent = false,
                errorMessage = null
            )
        }
    }

    fun sendResetEmail() {
        val email = _uiState.value.email
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isEmailSent = false, errorMessage = null) }
            try {
                sendPasswordResetEmailUseCase(email)
                _uiState.update { it.copy(isLoading = false, isEmailSent = true) }
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

    class Factory(
        private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InputEmailViewModel::class.java)) {
                return InputEmailViewModel(sendPasswordResetEmailUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
