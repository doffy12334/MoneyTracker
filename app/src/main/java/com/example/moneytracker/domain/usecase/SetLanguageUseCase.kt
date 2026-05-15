package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.AppLanguage
import com.example.moneytracker.domain.repository.SettingsRepository

class SetLanguageUseCase(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(language: AppLanguage) {
        settingsRepository.setLanguage(language)
    }
}
