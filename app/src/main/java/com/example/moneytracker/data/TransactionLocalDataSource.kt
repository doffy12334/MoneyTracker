package com.example.moneytracker.data

import com.example.moneytracker.domain.model.transaction.Transaction

interface TransactionLocalDataSource {
    suspend fun getTransactions(): List<Transaction>
    suspend fun saveTransactions(transactions: List<Transaction>)
    suspend fun addTransaction(transaction: Transaction)
}
