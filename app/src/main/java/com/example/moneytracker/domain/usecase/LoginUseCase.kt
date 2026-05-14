package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String) {
        val normalizedEmail = email.trim()

        require(normalizedEmail.isNotBlank()) { "Email khong duoc de trong" }
        require(password.isNotBlank()) { "Mat khau khong duoc de trong" }
        authRepository.login(normalizedEmail, password)
    }
}
