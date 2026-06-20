package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.AuthRepository

class VerifyPhoneOtpUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(verificationId: String, code: String) {
        require(code.length == 6) { "Mã OTP phải có 6 chữ số" }
        authRepository.verifyPhoneOtp(verificationId, code)
    }
}
