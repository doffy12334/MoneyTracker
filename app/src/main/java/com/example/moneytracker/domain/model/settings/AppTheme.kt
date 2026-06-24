package com.example.moneytracker.domain.model.settings


enum class AppTheme(
    val value: String
) {
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromValue(value: String): AppTheme {
            return entries.firstOrNull { it.value == value } ?: LIGHT
        }
    }
}
