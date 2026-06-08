package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.AuthRepository

class DeleteAccountUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(password: String) {
        authRepository.deleteAccount(password)
    }
}
