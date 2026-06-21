package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.exception.AppException
import com.example.moneytracker.R

object AuthAccountNormalizer {
    private const val INTERNAL_ACCOUNT_DOMAIN = "moneytracker.local"
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    private val USERNAME_REGEX = Regex("^[A-Za-z0-9._-]{3,32}$")

    fun normalize(account: String): String {
        val normalized = account.trim().lowercase()
        if (normalized.isBlank()) throw AppException(R.string.error_empty_account)
        return if ("@" in normalized) {
            if (!EMAIL_REGEX.matches(normalized)) throw AppException(R.string.error_invalid_email)
            normalized
        } else {
            if (!USERNAME_REGEX.matches(normalized)) {
                throw AppException(R.string.error_invalid_username_format)
            }
            "$normalized@$INTERNAL_ACCOUNT_DOMAIN"
        }
    }
}
