package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String) {
        val normalizedEmail = email.trim()

        require(normalizedEmail.isNotBlank()) { "Email không được để trống" }
        require(EMAIL_REGEX.matches(normalizedEmail)) { "Email không hợp lệ" }
        require(password.isNotBlank()) { "Mật khẩu không được để trống" }
        authRepository.login(normalizedEmail, password)
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    }
}
