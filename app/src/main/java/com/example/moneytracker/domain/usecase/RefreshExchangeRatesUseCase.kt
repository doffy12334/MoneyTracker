package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.ExchangeRateCache
import com.example.moneytracker.domain.repository.ExchangeRateRepository

class RefreshExchangeRatesUseCase(
    private val exchangeRateRepository: ExchangeRateRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false) {
        ExchangeRateCache.update(exchangeRateRepository.getVndPerUnitRates(forceRefresh))
    }
}
