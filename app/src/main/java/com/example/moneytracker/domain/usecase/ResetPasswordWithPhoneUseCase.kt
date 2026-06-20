package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.AuthRepository

class ResetPasswordWithPhoneUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(newPassword: String, confirmPassword: String) {
        require(newPassword.length >= MIN_PASSWORD_LENGTH) {
            "Mật khẩu phải có ít nhất $MIN_PASSWORD_LENGTH ký tự"
        }
        require(newPassword == confirmPassword) { "Mật khẩu xác nhận không khớp" }
        authRepository.resetPasswordAfterPhoneVerification(newPassword)
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
