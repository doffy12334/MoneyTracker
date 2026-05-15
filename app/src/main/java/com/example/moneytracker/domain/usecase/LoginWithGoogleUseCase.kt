package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.AuthRepository

class LoginWithGoogleUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String) {
        require(idToken.isNotBlank()) { "Không lấy được tài khoản Google" }
        authRepository.loginWithGoogle(idToken)
    }
}
