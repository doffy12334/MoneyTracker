package com.example.moneytracker.data

import com.example.moneytracker.domain.model.transaction.Transaction

class InMemoryTransactionLocalDataSource : TransactionLocalDataSource {
    private val transactions = mutableListOf<Transaction>()

    override suspend fun getTransactions(): List<Transaction> = transactions.toList()

    override suspend fun saveTransactions(transactions: List<Transaction>) {
        this.transactions.clear()
        this.transactions.addAll(transactions)
    }

    override suspend fun addTransaction(transaction: Transaction) {
        transactions.add(0, transaction)
    }
}
