package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.AuthRepository

class UpdatePasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(newPassword: String) {
        val normalized = newPassword.trim()
        require(normalized.length >= MIN_PASSWORD_LENGTH) {
            "Mật khẩu phải có ít nhất $MIN_PASSWORD_LENGTH ký tự"
        }
        authRepository.updatePassword(normalized)
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
