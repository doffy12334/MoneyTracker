package com.example.moneytracker.presentation.uistate

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Authenticated : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
