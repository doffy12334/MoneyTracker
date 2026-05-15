package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moneytracker.domain.model.AppLanguage
import com.example.moneytracker.domain.model.AppTheme
import com.example.moneytracker.domain.usecase.GetSettingsUseCase
import com.example.moneytracker.domain.usecase.SetLanguageUseCase
import com.example.moneytracker.domain.usecase.SetNotificationsEnabledUseCase
import com.example.moneytracker.domain.usecase.SetThemeUseCase
import com.example.moneytracker.presentation.uistate.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val setNotificationsEnabledUseCase: SetNotificationsEnabledUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val setThemeUseCase: SetThemeUseCase
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
            theme = settings.theme
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

    class Factory(
        private val getSettingsUseCase: GetSettingsUseCase,
        private val setNotificationsEnabledUseCase: SetNotificationsEnabledUseCase,
        private val setLanguageUseCase: SetLanguageUseCase,
        private val setThemeUseCase: SetThemeUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(
                    getSettingsUseCase,
                    setNotificationsEnabledUseCase,
                    setLanguageUseCase,
                    setThemeUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
