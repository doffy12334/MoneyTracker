package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.AppCurrency
import com.example.moneytracker.domain.repository.SettingsRepository

class SetCurrencyUseCase(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(currency: AppCurrency) {
        settingsRepository.setCurrency(currency)
    }
}
