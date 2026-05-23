package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.SecuritySettingsRepository

class SetBiometricEnabledUseCase(
    private val securitySettingsRepository: SecuritySettingsRepository
) {
    operator fun invoke(enabled: Boolean) {
        securitySettingsRepository.setBiometricEnabled(enabled)
    }
}
