package com.example.moneytracker.domain.model

object ExchangeRateCache {
    @Volatile
    private var vndPerUnitRates: Map<AppCurrency, Double> = AppCurrency.entries.associateWith {
        it.fallbackVndPerUnit
    }

    fun update(rates: Map<AppCurrency, Double>) {
        vndPerUnitRates = AppCurrency.entries.associateWith { currency ->
            rates[currency]?.takeIf { it > 0.0 } ?: currency.fallbackVndPerUnit
        }
    }

    fun vndPerUnit(currency: AppCurrency): Double {
        return vndPerUnitRates[currency]?.takeIf { it > 0.0 } ?: currency.fallbackVndPerUnit
    }
}
