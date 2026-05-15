package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.SettingsRepository

class SetNotificationsEnabledUseCase(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(enabled: Boolean) {
        settingsRepository.setNotificationsEnabled(enabled)
    }
}
