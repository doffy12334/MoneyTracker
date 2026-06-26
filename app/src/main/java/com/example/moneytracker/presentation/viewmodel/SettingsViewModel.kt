package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moneytracker.domain.model.settings.AppCurrency
import com.example.moneytracker.domain.model.settings.AppLanguage
import com.example.moneytracker.domain.model.settings.AppTheme
import com.example.moneytracker.domain.usecase.GetSettingsUseCase
import com.example.moneytracker.domain.usecase.LogoutUseCase
import com.example.moneytracker.domain.usecase.RefreshExchangeRatesUseCase
import com.example.moneytracker.domain.usecase.SetCurrencyUseCase
import com.example.moneytracker.domain.usecase.SetLanguageUseCase
import com.example.moneytracker.domain.usecase.SetNotificationsEnabledUseCase
import com.example.moneytracker.domain.usecase.SetThemeUseCase
import com.example.moneytracker.presentation.uistate.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val setNotificationsEnabledUseCase: SetNotificationsEnabledUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val setCurrencyUseCase: SetCurrencyUseCase,
    private val refreshExchangeRatesUseCase: RefreshExchangeRatesUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        val settings = getSettingsUseCase()
        _uiState.value = SettingsUiState(
            notificationsEnabled = settings.notificationsEnabled,
            language = settings.language,
            theme = settings.theme,
            currency = settings.currency
        )
    }

    fun onNotificationsChanged(enabled: Boolean) {
        setNotificationsEnabledUseCase(enabled)
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun onLanguageChanged(language: AppLanguage) {
        setLanguageUseCase(language)
        _uiState.update { it.copy(language = language) }
    }

    fun onThemeChanged(theme: AppTheme) {
        setThemeUseCase(theme)
        _uiState.update { it.copy(theme = theme) }
    }

    fun onCurrencyChanged(currency: AppCurrency) {
        setCurrencyUseCase(currency)
        viewModelScope.launch {
            refreshExchangeRatesUseCase()
            _uiState.update { it.copy(currency = currency) }
        }
    }

    fun logout() {
        logoutUseCase()
    }

    class Factory(
        private val getSettingsUseCase: GetSettingsUseCase,
        private val setNotificationsEnabledUseCase: SetNotificationsEnabledUseCase,
        private val setLanguageUseCase: SetLanguageUseCase,
        private val setThemeUseCase: SetThemeUseCase,
        private val setCurrencyUseCase: SetCurrencyUseCase,
        private val refreshExchangeRatesUseCase: RefreshExchangeRatesUseCase,
        private val logoutUseCase: LogoutUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(
                    getSettingsUseCase,
                    setNotificationsEnabledUseCase,
                    setLanguageUseCase,
                    setThemeUseCase,
                    setCurrencyUseCase,
                    refreshExchangeRatesUseCase,
                    logoutUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
