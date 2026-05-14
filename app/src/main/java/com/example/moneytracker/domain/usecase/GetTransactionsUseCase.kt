package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.transaction.Transaction
import com.example.moneytracker.domain.repository.TransactionRepository

class GetTransactionsUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(): List<Transaction> = repository.getTransactions()

}