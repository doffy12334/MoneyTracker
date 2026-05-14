package com.example.moneytracker.data

import com.example.moneytracker.domain.model.transaction.Transaction
import com.example.moneytracker.domain.repository.TransactionRepository

class TransactionRepositoryImp(
    private val remoteDataSource: TransactionRemoteDataSource,
    private val localDataSource: TransactionLocalDataSource
) : TransactionRepository{
    override suspend fun getTransactions() :List<Transaction>{
        return try{
            val remote = remoteDataSource.fetchTransactions()
            localDataSource.saveTransactions(remote)
            remote
        } catch (e: Exception){
            localDataSource.getTransactions()
        }
    }

    override suspend fun addTransaction(transaction: Transaction) {
        remoteDataSource.pushTransaction(transaction)
        localDataSource.addTransaction(transaction)
    }
}