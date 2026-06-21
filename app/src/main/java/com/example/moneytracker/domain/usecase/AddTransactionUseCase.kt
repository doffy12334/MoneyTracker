package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.transaction.Transaction
import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.model.transaction.TransactionType
import com.example.moneytracker.domain.repository.TransactionRepository
import com.example.moneytracker.domain.exception.AppException
import com.example.moneytracker.R
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddTransactionUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        name: String,
        amount: Double,
        date: String,
        category: TransactionCategory,
        customCategory: String? = null,
        type: TransactionType
    ) {
        if (name.isBlank()) throw AppException(R.string.error_empty_transaction_name)
        if (amount <= 0.0) throw AppException(R.string.error_amount_must_be_positive)
        if (date.isBlank()) throw AppException(R.string.error_empty_transaction_date)

        transactionRepository.addTransaction(
            Transaction(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                amount = amount,
                date = date.trim(),
                category = category,
                customCategory = customCategory?.trim()?.takeIf { it.isNotBlank() },
                type = type,
                createdAt = SimpleDateFormat(CREATED_AT_PATTERN, Locale.US).format(Date())
            )
        )
    }

    private companion object {
        const val CREATED_AT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS"
    }
}
