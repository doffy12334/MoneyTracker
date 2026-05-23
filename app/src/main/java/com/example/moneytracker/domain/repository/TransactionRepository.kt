package com.example.moneytracker.domain.repository

import com.example.moneytracker.domain.model.transaction.Transaction

interface TransactionRepository {
    suspend fun getTransactions(): List<Transaction>
    suspend fun addTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transactionId: String)
}
