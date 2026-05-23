package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.TransactionRepository

class DeleteTransactionUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transactionId: String) {
        require(transactionId.isNotBlank()) { "Ma giao dich khong hop le" }
        transactionRepository.deleteTransaction(transactionId)
    }
}
