package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.AuthRepository

class LoginWithGoogleUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String) {
        require(idToken.isNotBlank()) { "KhĂ´ng láº¥y Ä‘Æ°á»£c tĂ i khoáº£n Google" }
        authRepository.loginWithGoogle(idToken)
    }
}
