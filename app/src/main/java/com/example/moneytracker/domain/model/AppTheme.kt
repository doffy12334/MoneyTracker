package com.example.moneytracker.domain.model

enum class AppTheme(
    val value: String,
    val displayName: String
) {
    LIGHT("light", "SĂ¡ng"),
    DARK("dark", "Tá»‘i");

    companion object {
        fun fromValue(value: String): AppTheme {
            return entries.firstOrNull { it.value == value } ?: LIGHT
        }
    }
}
