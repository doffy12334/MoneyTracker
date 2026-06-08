package com.example.moneytracker.domain.repository

import com.example.moneytracker.domain.model.AppCurrency

interface ExchangeRateRepository {
    suspend fun getVndPerUnitRates(forceRefresh: Boolean = false): Map<AppCurrency, Double>
}
