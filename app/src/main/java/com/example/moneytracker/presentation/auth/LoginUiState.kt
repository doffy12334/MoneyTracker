package com.example.moneytracker.presentation.auth

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Authenticated : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
