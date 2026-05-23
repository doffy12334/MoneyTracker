package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.SecuritySettingsRepository

class GetSecuritySettingsUseCase(
    private val securitySettingsRepository: SecuritySettingsRepository
) {
    operator fun invoke() = securitySettingsRepository.getSecuritySettings()
}
