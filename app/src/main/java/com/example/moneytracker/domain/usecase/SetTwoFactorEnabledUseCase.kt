package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.SecuritySettingsRepository

class SetTwoFactorEnabledUseCase(
    private val securitySettingsRepository: SecuritySettingsRepository
) {
    operator fun invoke(enabled: Boolean) {
        securitySettingsRepository.setTwoFactorEnabled(enabled)
    }
}
