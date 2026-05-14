package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.AuthRepository

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, confirmPassword: String) {
        val normalizedEmail = email.trim()

        require(normalizedEmail.isNotBlank()) { "Email khong duoc de trong" }
        require(EMAIL_REGEX.matches(normalizedEmail)) { "Email khong hop le" }
        require(normalizedEmail.substringAfter("@") !in BLACKLISTED_DOMAINS) {
            "Khong ho tro email tam thoi"
        }
        require(password.length >= MIN_PASSWORD_LENGTH) {
            "Mat khau phai co it nhat $MIN_PASSWORD_LENGTH ky tu"
        }
        require(password == confirmPassword) { "Mat khau xac nhan khong khop" }

        authRepository.register(normalizedEmail, password)
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        val BLACKLISTED_DOMAINS = setOf("yopmail.com", "tempmail.com", "mailinator.com")
    }
}
