package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.settings.AppTheme
import com.example.moneytracker.domain.repository.SettingsRepository

class SetThemeUseCase(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(theme: AppTheme) {
        settingsRepository.setTheme(theme)
    }
}
