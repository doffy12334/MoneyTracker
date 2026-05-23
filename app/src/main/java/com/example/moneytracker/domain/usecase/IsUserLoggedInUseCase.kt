package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.AuthRepository

class IsUserLoggedInUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Boolean = authRepository.isUserLoggedIn()
}
