package com.example.moneytracker.di

import android.content.Context
import com.example.moneytracker.data.SharedPreferencesSettingsRepository
import com.example.moneytracker.data.FakeTransactionRemoteDataSource
import com.example.moneytracker.data.FirebaseAuthRepository
import com.example.moneytracker.data.InMemoryTransactionLocalDataSource
import com.example.moneytracker.data.TransactionRepositoryImp
import com.example.moneytracker.data.local.SharedPrefsManager
import com.example.moneytracker.domain.repository.AuthRepository
import com.example.moneytracker.domain.repository.SettingsRepository
import com.example.moneytracker.domain.repository.TransactionRepository
import com.example.moneytracker.domain.usecase.AddTransactionUseCase
import com.example.moneytracker.domain.usecase.GetDashboardSummaryUseCase
import com.example.moneytracker.domain.usecase.GetSettingsUseCase
import com.example.moneytracker.domain.usecase.GetTransactionsUseCase
import com.example.moneytracker.domain.usecase.LoginUseCase
import com.example.moneytracker.domain.usecase.RegisterUseCase
import com.example.moneytracker.domain.usecase.SendPasswordResetEmailUseCase
import com.example.moneytracker.domain.usecase.SetLanguageUseCase
import com.example.moneytracker.domain.usecase.SetNotificationsEnabledUseCase
import com.example.moneytracker.domain.usecase.SetThemeUseCase
import com.example.moneytracker.domain.usecase.VerifyPasswordResetCodeUseCase

object AppContainer {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

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

    private val sharedPrefsManager: SharedPrefsManager by lazy {
        SharedPrefsManager(appContext)
    }

    private val settingsRepository: SettingsRepository by lazy {
        SharedPreferencesSettingsRepository(sharedPrefsManager)
    }

    val getSettingsUseCase: GetSettingsUseCase by lazy {
        GetSettingsUseCase(settingsRepository)
    }

    val setNotificationsEnabledUseCase: SetNotificationsEnabledUseCase by lazy {
        SetNotificationsEnabledUseCase(settingsRepository)
    }

    val setLanguageUseCase: SetLanguageUseCase by lazy {
        SetLanguageUseCase(settingsRepository)
    }

    val setThemeUseCase: SetThemeUseCase by lazy {
        SetThemeUseCase(settingsRepository)
    }
}
