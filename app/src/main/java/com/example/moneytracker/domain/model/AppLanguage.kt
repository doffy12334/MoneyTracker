package com.example.moneytracker.domain.model

enum class AppLanguage(
    val code: String,
    val displayName: String
) {
    VIETNAMESE("vi", "Tiếng Việt"),
    ENGLISH("en", "English"),
    FRENCH("fr", "Français");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code == code } ?: VIETNAMESE
        }
    }
}
