package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.AppSettings
import com.example.moneytracker.domain.repository.SettingsRepository

class GetSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): AppSettings = settingsRepository.getSettings()
}
