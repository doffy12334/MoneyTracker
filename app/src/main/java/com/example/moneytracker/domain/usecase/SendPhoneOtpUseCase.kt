package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.AuthRepository

class SendPhoneOtpUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(phoneNumber: String, activity: Any): String {
        val normalized = phoneNumber.trim()
        require(normalized.isNotBlank()) { "Số điện thoại không được để trống" }
        require(normalized.startsWith("+")) { "Số điện thoại phải có mã quốc gia (VD: +84...)" }
        return authRepository.sendPhoneVerificationCode(normalized, activity)
    }
}
