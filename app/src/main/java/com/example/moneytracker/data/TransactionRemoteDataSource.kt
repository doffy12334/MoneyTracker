package com.example.moneytracker.data

import com.example.moneytracker.domain.model.transaction.Transaction

interface TransactionRemoteDataSource {
    suspend fun fetchTransactions(): List<Transaction>
    suspend fun pushTransaction(transaction: Transaction)
}