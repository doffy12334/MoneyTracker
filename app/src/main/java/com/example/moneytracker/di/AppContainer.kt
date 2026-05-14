package com.example.moneytracker.di

import com.example.moneytracker.data.FakeTransactionRemoteDataSource
import com.example.moneytracker.data.FirebaseAuthRepository
import com.example.moneytracker.data.InMemoryTransactionLocalDataSource
import com.example.moneytracker.data.TransactionRepositoryImp
import com.example.moneytracker.domain.repository.AuthRepository
import com.example.moneytracker.domain.repository.TransactionRepository
import com.example.moneytracker.domain.usecase.AddTransactionUseCase
import com.example.moneytracker.domain.usecase.GetDashboardSummaryUseCase
import com.example.moneytracker.domain.usecase.GetTransactionsUseCase
import com.example.moneytracker.domain.usecase.LoginUseCase
import com.example.moneytracker.domain.usecase.RegisterUseCase
import com.example.moneytracker.domain.usecase.SendPasswordResetEmailUseCase
import com.example.moneytracker.domain.usecase.VerifyPasswordResetCodeUseCase

object AppContainer {
    private val localTransactionDataSource = InMemoryTransactionLocalDataSource()
    private val remoteTransactionDataSource = FakeTransactionRemoteDataSource()

    private val transactionRepository: TransactionRepository by lazy {
        TransactionRepositoryImp(
            remoteDataSource = remoteTransactionDataSource,
            localDataSource = localTransactionDataSource
        )
    }

    val getTransactionsUseCase: GetTransactionsUseCase by lazy {
        GetTransactionsUseCase(transactionRepository)
    }

    val addTransactionUseCase: AddTransactionUseCase by lazy {
        AddTransactionUseCase(transactionRepository)
    }

    val getDashboardSummaryUseCase: GetDashboardSummaryUseCase by lazy {
        GetDashboardSummaryUseCase(transactionRepository)
    }

    private val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository()
    }

    val loginUseCase: LoginUseCase by lazy {
        LoginUseCase(authRepository)
    }

    val registerUseCase: RegisterUseCase by lazy {
        RegisterUseCase(authRepository)
    }

    val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase by lazy {
        SendPasswordResetEmailUseCase(authRepository)
    }

    val verifyPasswordResetCodeUseCase: VerifyPasswordResetCodeUseCase by lazy {
        VerifyPasswordResetCodeUseCase(authRepository)
    }
}
