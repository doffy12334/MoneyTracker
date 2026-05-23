package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.AuthRepository

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, confirmPassword: String) {
        val normalizedEmail = email.trim()

        require(normalizedEmail.isNotBlank()) { "Email khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng" }
        require(EMAIL_REGEX.matches(normalizedEmail)) { "Email khĂ´ng há»£p lá»‡" }
        require(normalizedEmail.substringAfter("@") !in BLACKLISTED_DOMAINS) {
            "KhĂ´ng há»— trá»£ email táº¡m thá»i"
        }
        require(password.length >= MIN_PASSWORD_LENGTH) {
            "Máº­t kháº©u pháº£i cĂ³ Ă­t nháº¥t $MIN_PASSWORD_LENGTH kĂ½ tá»±"
        }
        require(password == confirmPassword) { "Máº­t kháº©u xĂ¡c nháº­n khĂ´ng khá»›p" }

        authRepository.register(normalizedEmail, password)
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        val BLACKLISTED_DOMAINS = setOf("yopmail.com", "tempmail.com", "mailinator.com")
    }
}
