package com.example.moneytracker.domain.model

enum class AppCurrency(
    val code: String,
    val localeTag: String,
    val fractionDigits: Int,
    val fallbackVndPerUnit: Double
) {
    VND("VND", "vi-VN", 0, 1.0),
    USD("USD", "en-US", 2, 26_353.8),
    EUR("EUR", "de-DE", 2, 30_638.0),
    JPY("JPY", "ja-JP", 0, 165.1315);

    fun fromVnd(amountInVnd: Double): Double = amountInVnd / ExchangeRateCache.vndPerUnit(this)

    fun toVnd(amount: Double): Double = amount * ExchangeRateCache.vndPerUnit(this)

    companion object {
        fun fromCode(code: String): AppCurrency {
            return entries.firstOrNull { it.code == code } ?: VND
        }
    }
}
