package com.example.moneytracker.domain.model

enum class AppLanguage(
    val code: String,
    val displayName: String
) {
    VIETNAMESE("vi", "Tiáº¿ng Viá»‡t"),
    ENGLISH("en", "English");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code == code } ?: VIETNAMESE
        }
    }
}
