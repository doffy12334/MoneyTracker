package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.SecuritySettingsRepository

class SetHighValueProtectionEnabledUseCase(
    private val securitySettingsRepository: SecuritySettingsRepository
) {
    operator fun invoke(enabled: Boolean) {
        securitySettingsRepository.setHighValueProtectionEnabled(enabled)
    }
}
