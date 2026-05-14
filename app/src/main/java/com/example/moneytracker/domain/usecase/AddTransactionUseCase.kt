package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.transaction.Transaction
import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.model.transaction.TransactionType
import com.example.moneytracker.domain.repository.TransactionRepository
import java.util.UUID

class AddTransactionUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        name: String,
        amount: Double,
        date: String,
        category: TransactionCategory,
        type: TransactionType
    ) {
        require(name.isNotBlank()) { "Ten giao dich khong duoc de trong" }
        require(amount > 0.0) { "So tien phai lon hon 0" }
        require(date.isNotBlank()) { "Ngay giao dich khong duoc de trong" }

        transactionRepository.addTransaction(
            Transaction(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                amount = amount,
                date = date.trim(),
                category = category,
                type = type
            )
        )
    }
}
