package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.AuthRepository

class SendPasswordResetEmailUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String) {
        val normalizedEmail = email.trim()

        require(normalizedEmail.isNotBlank()) { "Email khong duoc de trong" }
        authRepository.sendPasswordResetEmail(normalizedEmail)
    }
}
