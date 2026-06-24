package com.example.moneytracker.domain.model

enum class AppLanguage(
    val code: String
) {
    VIETNAMESE("vi"),
    ENGLISH("en"),
    FRENCH("fr");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code == code } ?: VIETNAMESE
        }
    }
}
