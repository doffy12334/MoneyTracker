package com.example.moneytracker.presentation.util

import com.example.moneytracker.domain.model.AppCurrency
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {
    fun create(appCurrency: AppCurrency): NumberFormat {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag(appCurrency.localeTag)).apply {
            currency = Currency.getInstance(appCurrency.code)
            minimumFractionDigits = appCurrency.fractionDigits
            maximumFractionDigits = appCurrency.fractionDigits
        }
    }

    fun formatFromVnd(amountInVnd: Double, appCurrency: AppCurrency): String {
        return create(appCurrency).format(appCurrency.fromVnd(amountInVnd))
    }
}
