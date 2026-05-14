package com.example.moneytracker.presentation.uistate

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Registered : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}
