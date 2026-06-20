package com.example.moneytracker.domain.model.transaction

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Transaction.isInCurrentMonth(): Boolean {
    val parsedDate = parseDate(date) ?: parseDate(createdAt) ?: return false
    val current = Calendar.getInstance()
    val transactionDate = Calendar.getInstance().apply { time = parsedDate }
    return current.get(Calendar.YEAR) == transactionDate.get(Calendar.YEAR) &&
        current.get(Calendar.MONTH) == transactionDate.get(Calendar.MONTH)
}

private fun parseDate(value: String): Date? {
    val trimmedValue = value.trim()
    if (trimmedValue.isBlank()) return null
    return DATE_PATTERNS.firstNotNullOfOrNull { pattern ->
        DATE_LOCALES.firstNotNullOfOrNull { locale ->
            runCatching {
                SimpleDateFormat(pattern, locale).apply {
                    isLenient = false
                }.parse(trimmedValue)
            }.getOrNull()
        }
    }
}

private val DATE_LOCALES = listOf(Locale.US, Locale.ENGLISH, Locale.forLanguageTag("vi-VN"))
private val DATE_PATTERNS = listOf(
    "dd MMM yyyy",
    "d MMM yyyy",
    "dd MMMM yyyy",
    "d MMMM yyyy",
    "dd/MM/yyyy",
    "d/M/yyyy",
    "dd-MM-yyyy",
    "d-M-yyyy",
    "yyyy-MM-dd",
    "yyyy-MM-dd HH:mm:ss.SSS",
    "EEE MMM dd HH:mm:ss zzz yyyy"
)
