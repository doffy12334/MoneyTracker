package com.example.moneytracker.data

import com.example.moneytracker.domain.model.transaction.Transaction
import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.model.transaction.TransactionType

class FakeTransactionRemoteDataSource : TransactionRemoteDataSource {
    private val transactions = mutableListOf(
        Transaction("1", "Luong thang 5", 5000.0, "10 May 2026", TransactionCategory.SALARY, TransactionType.INCOME),
        Transaction("2", "Mua cafe", 5.0, "10 May 2026", TransactionCategory.FOOD, TransactionType.EXPENSE),
        Transaction("3", "Tien dien", 100.0, "09 May 2026", TransactionCategory.OTHER, TransactionType.EXPENSE),
        Transaction("4", "Mua ao", 100.0, "09 May 2026", TransactionCategory.SHOPPING, TransactionType.EXPENSE),
        Transaction("5", "Di chuyen", 20.0, "08 May 2026", TransactionCategory.TRANSPORT, TransactionType.EXPENSE)
    )

    override suspend fun fetchTransactions(): List<Transaction> = transactions.toList()

    override suspend fun pushTransaction(transaction: Transaction) {
        transactions.add(0, transaction)
    }

    override suspend fun deleteTransaction(transactionId: String) {
        transactions.removeAll { it.id == transactionId }
    }
}
