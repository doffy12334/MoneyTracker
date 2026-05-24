package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String) {
        val normalizedAccount = AuthAccountNormalizer.normalize(email)
        require(password.isNotBlank()) { "Mật khẩu không được để trống" }
        authRepository.login(normalizedAccount, password)
    }
}
