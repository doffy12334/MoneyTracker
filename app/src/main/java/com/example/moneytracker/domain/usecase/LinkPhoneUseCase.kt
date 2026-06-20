package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.AuthRepository

class LinkPhoneUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(verificationId: String, code: String) {
        require(code.length == 6) { "Mã OTP phải có 6 chữ số" }
        authRepository.linkPhoneToCurrentUser(verificationId, code)
    }
}
