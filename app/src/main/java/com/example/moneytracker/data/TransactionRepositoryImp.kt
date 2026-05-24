package com.example.moneytracker.data

import com.example.moneytracker.domain.model.transaction.Transaction
import com.example.moneytracker.domain.repository.TransactionRepository

class TransactionRepositoryImp(
    private val remoteDataSource: TransactionRemoteDataSource,
    private val localDataSource: TransactionLocalDataSource
) : TransactionRepository{
    override suspend fun getTransactions() :List<Transaction>{
        val local = localDataSource.getTransactions()
        return try{
            val remote = remoteDataSource.fetchTransactions()
            val remoteIds = remote.map { it.id }.toSet()
            val localOnly = local.filterNot { it.id in remoteIds }
            localOnly.forEach { transaction ->
                runCatching { remoteDataSource.pushTransaction(transaction) }
            }
            val merged = remote + localOnly
            localDataSource.saveTransactions(merged)
            merged
        } catch (e: Exception){
            local
        }
    }

    override suspend fun addTransaction(transaction: Transaction) {
        localDataSource.addTransaction(transaction)
        remoteDataSource.pushTransaction(transaction)
    }

    override suspend fun deleteTransaction(transactionId: String) {
        remoteDataSource.deleteTransaction(transactionId)
        localDataSource.deleteTransaction(transactionId)
    }
}
