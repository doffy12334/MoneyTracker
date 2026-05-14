package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.moneytracker.presentation.uistate.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onCurrencyChanged(currencyCode: String) {
        _uiState.update { it.copy(currencyCode = currencyCode) }
    }

    fun onDarkModeChanged(isDarkMode: Boolean) {
        _uiState.update { it.copy(isDarkMode = isDarkMode) }
    }
}
