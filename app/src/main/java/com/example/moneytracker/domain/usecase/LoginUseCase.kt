package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String) {
        val normalizedEmail = email.trim()

        require(normalizedEmail.isNotBlank()) { "Email khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng" }
        require(EMAIL_REGEX.matches(normalizedEmail)) { "Email khĂ´ng há»£p lá»‡" }
        require(password.isNotBlank()) { "Máº­t kháº©u khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng" }
        authRepository.login(normalizedEmail, password)
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    }
}
