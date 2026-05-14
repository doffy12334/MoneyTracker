package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.AuthRepository

class VerifyPasswordResetCodeUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(code: String): String {
        require(code.isNotBlank()) { "Ma xac thuc khong duoc de trong" }
        return authRepository.verifyPasswordResetCode(code)
    }
}
