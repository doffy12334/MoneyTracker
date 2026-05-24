package com.example.moneytracker.domain.model

enum class AppCurrency(
    val code: String,
    val localeTag: String,
    val fractionDigits: Int
) {
    VND("VND", "vi-VN", 0),
    USD("USD", "en-US", 2),
    EUR("EUR", "de-DE", 2),
    JPY("JPY", "ja-JP", 0);

    companion object {
        fun fromCode(code: String): AppCurrency {
            return entries.firstOrNull { it.code == code } ?: VND
        }
    }
}
