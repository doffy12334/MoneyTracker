package com.example.moneytracker.di

import android.content.Context
import com.example.moneytracker.data.InternalStorageExportReportRepository
import com.example.moneytracker.data.SharedPreferencesSettingsRepository
import com.example.moneytracker.data.SharedPreferencesProfileRepository
import com.example.moneytracker.data.SharedPreferencesSecuritySettingsRepository
import com.example.moneytracker.data.FirebaseAuthRepository
import com.example.moneytracker.data.FirebaseTransactionRemoteDataSource
import com.example.moneytracker.data.SharedPreferencesTransactionLocalDataSource
import com.example.moneytracker.data.TransactionRepositoryImp
import com.example.moneytracker.data.local.SharedPrefsManager
import com.example.moneytracker.domain.repository.AuthRepository
import com.example.moneytracker.domain.repository.ExportReportRepository
import com.example.moneytracker.domain.repository.ProfileRepository
import com.example.moneytracker.domain.repository.SecuritySettingsRepository
import com.example.moneytracker.domain.repository.SettingsRepository
import com.example.moneytracker.domain.repository.TransactionRepository
import com.example.moneytracker.domain.usecase.AddTransactionUseCase
import com.example.moneytracker.domain.usecase.ExportReportUseCase
import com.example.moneytracker.domain.usecase.GetDashboardSummaryUseCase
import com.example.moneytracker.domain.usecase.GetProfileUseCase
import com.example.moneytracker.domain.usecase.GetSecuritySettingsUseCase
import com.example.moneytracker.domain.usecase.GetSettingsUseCase
import com.example.moneytracker.domain.usecase.GetSpendingReportUseCase
import com.example.moneytracker.domain.usecase.GetTransactionsUseCase
import com.example.moneytracker.domain.usecase.IsUserLoggedInUseCase
import com.example.moneytracker.domain.usecase.LoginUseCase
import com.example.moneytracker.domain.usecase.LoginWithGoogleUseCase
import com.example.moneytracker.domain.usecase.LogoutUseCase
import com.example.moneytracker.domain.usecase.RegisterUseCase
import com.example.moneytracker.domain.usecase.SendPasswordResetEmailUseCase
import com.example.moneytracker.domain.usecase.SetBiometricEnabledUseCase
import com.example.moneytracker.domain.usecase.SetHighValueProtectionEnabledUseCase
import com.example.moneytracker.domain.usecase.SetLanguageUseCase
import com.example.moneytracker.domain.usecase.SetNotificationsEnabledUseCase
import com.example.moneytracker.domain.usecase.SetThemeUseCase
import com.example.moneytracker.domain.usecase.SetTwoFactorEnabledUseCase
import com.example.moneytracker.domain.usecase.UpdateProfileUseCase
import com.example.moneytracker.domain.usecase.VerifyPasswordResetCodeUseCase

object AppContainer {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val localTransactionDataSource by lazy {
        SharedPreferencesTransactionLocalDataSource(appContext)
    }
    private val remoteTransactionDataSource = FirebaseTransactionRemoteDataSource()

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

    val getSpendingReportUseCase: GetSpendingReportUseCase by lazy {
        GetSpendingReportUseCase(transactionRepository)
    }

    private val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository()
    }

    val loginUseCase: LoginUseCase by lazy {
        LoginUseCase(authRepository)
    }

    val isUserLoggedInUseCase: IsUserLoggedInUseCase by lazy {
        IsUserLoggedInUseCase(authRepository)
    }

    val logoutUseCase: LogoutUseCase by lazy {
        LogoutUseCase(authRepository)
    }

    val loginWithGoogleUseCase: LoginWithGoogleUseCase by lazy {
        LoginWithGoogleUseCase(authRepository)
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

    private val profileRepository: ProfileRepository by lazy {
        SharedPreferencesProfileRepository(sharedPrefsManager)
    }

    private val securitySettingsRepository: SecuritySettingsRepository by lazy {
        SharedPreferencesSecuritySettingsRepository(sharedPrefsManager)
    }

    private val exportReportRepository: ExportReportRepository by lazy {
        InternalStorageExportReportRepository(appContext)
    }

    val exportReportUseCase: ExportReportUseCase by lazy {
        ExportReportUseCase(transactionRepository, exportReportRepository)
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

    val getProfileUseCase: GetProfileUseCase by lazy {
        GetProfileUseCase(profileRepository)
    }

    val updateProfileUseCase: UpdateProfileUseCase by lazy {
        UpdateProfileUseCase(profileRepository)
    }

    val getSecuritySettingsUseCase: GetSecuritySettingsUseCase by lazy {
        GetSecuritySettingsUseCase(securitySettingsRepository)
    }

    val setTwoFactorEnabledUseCase: SetTwoFactorEnabledUseCase by lazy {
        SetTwoFactorEnabledUseCase(securitySettingsRepository)
    }

    val setBiometricEnabledUseCase: SetBiometricEnabledUseCase by lazy {
        SetBiometricEnabledUseCase(securitySettingsRepository)
    }

    val setHighValueProtectionEnabledUseCase: SetHighValueProtectionEnabledUseCase by lazy {
        SetHighValueProtectionEnabledUseCase(securitySettingsRepository)
    }
}
