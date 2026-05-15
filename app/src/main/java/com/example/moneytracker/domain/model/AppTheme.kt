package com.example.moneytracker.domain.model

enum class AppTheme(
    val value: String,
    val displayName: String
) {
    LIGHT("light", "Sáng"),
    DARK("dark", "Tối");

    companion object {
        fun fromValue(value: String): AppTheme {
            return entries.firstOrNull { it.value == value } ?: LIGHT
        }
    }
}
