package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.AuthRepository

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, confirmPassword: String) {
        val normalizedEmail = email.trim()

        require(normalizedEmail.isNotBlank()) { "Email không được để trống" }
        require(EMAIL_REGEX.matches(normalizedEmail)) { "Email không hợp lệ" }
        require(normalizedEmail.substringAfter("@") !in BLACKLISTED_DOMAINS) {
            "Không hỗ trợ email tạm thời"
        }
        require(password.length >= MIN_PASSWORD_LENGTH) {
            "Mật khẩu phải có ít nhất $MIN_PASSWORD_LENGTH ký tự"
        }
        require(password == confirmPassword) { "Mật khẩu xác nhận không khớp" }

        authRepository.register(normalizedEmail, password)
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        val BLACKLISTED_DOMAINS = setOf("yopmail.com", "tempmail.com", "mailinator.com")
    }
}
