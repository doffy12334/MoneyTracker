package com.example.moneytracker.domain.usecase

object AuthAccountNormalizer {
    private const val INTERNAL_ACCOUNT_DOMAIN = "moneytracker.local"
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    private val USERNAME_REGEX = Regex("^[A-Za-z0-9._-]{3,32}$")

    fun normalize(account: String): String {
        val normalized = account.trim().lowercase()
        require(normalized.isNotBlank()) { "Tài khoản không được để trống" }
        return if ("@" in normalized) {
            require(EMAIL_REGEX.matches(normalized)) { "Email không hợp lệ" }
            normalized
        } else {
            require(USERNAME_REGEX.matches(normalized)) {
                "Tài khoản phải có 3-32 ký tự, chỉ gồm chữ, số, dấu chấm, gạch dưới hoặc gạch ngang"
            }
            "$normalized@$INTERNAL_ACCOUNT_DOMAIN"
        }
    }
}
