package com.example.moneytracker.di

import android.content.Context
import com.example.moneytracker.data.local.SharedPrefsManager
import com.example.moneytracker.data.local.SharedPreferencesTransactionLocalDataSource
import com.example.moneytracker.data.remote.FirebaseTransactionRemoteDataSource
import com.example.moneytracker.data.repository.FirebaseAuthRepository
import com.example.moneytracker.data.repository.InternalStorageExportReportRepository
import com.example.moneytracker.data.repository.OpenExchangeRateRepository
import com.example.moneytracker.data.repository.SharedPreferencesBudgetRepository
import com.example.moneytracker.data.repository.SharedPreferencesProfileRepository
import com.example.moneytracker.data.repository.SharedPreferencesSecuritySettingsRepository
import com.example.moneytracker.data.repository.SharedPreferencesSettingsRepository
import com.example.moneytracker.data.repository.TransactionRepositoryImp
import com.example.moneytracker.domain.repository.AuthRepository
import com.example.moneytracker.domain.repository.BudgetRepository
import com.example.moneytracker.domain.repository.ExchangeRateRepository
import com.example.moneytracker.domain.repository.ExportReportRepository
import com.example.moneytracker.domain.repository.ProfileRepository
import com.example.moneytracker.domain.repository.SecuritySettingsRepository
import com.example.moneytracker.domain.repository.SettingsRepository
import com.example.moneytracker.domain.repository.TransactionRepository
import com.example.moneytracker.domain.usecase.AddTransactionUseCase
import com.example.moneytracker.domain.usecase.DeleteAccountUseCase
import com.example.moneytracker.domain.usecase.DeleteBudgetLimitUseCase
import com.example.moneytracker.domain.usecase.DeleteSavingGoalUseCase
import com.example.moneytracker.domain.usecase.DeleteTransactionUseCase
import com.example.moneytracker.domain.usecase.ExportReportUseCase
import com.example.moneytracker.domain.usecase.GetBudgetOverviewUseCase
import com.example.moneytracker.domain.usecase.GetDashboardSummaryUseCase
import com.example.moneytracker.domain.usecase.GetProfileUseCase
import com.example.moneytracker.domain.usecase.GetSecuritySettingsUseCase
import com.example.moneytracker.domain.usecase.GetSettingsUseCase
import com.example.moneytracker.domain.usecase.GetSpendingReportUseCase
import com.example.moneytracker.domain.usecase.GetTransactionsUseCase
import com.example.moneytracker.domain.usecase.IsCurrentUserGoogleAccountUseCase
import com.example.moneytracker.domain.usecase.IsUserLoggedInUseCase
import com.example.moneytracker.domain.usecase.LinkPhoneUseCase
import com.example.moneytracker.domain.usecase.LoginUseCase
import com.example.moneytracker.domain.usecase.LoginWithGoogleUseCase
import com.example.moneytracker.domain.usecase.LogoutUseCase
import com.example.moneytracker.domain.usecase.RefreshExchangeRatesUseCase
import com.example.moneytracker.domain.usecase.RegisterUseCase
import com.example.moneytracker.domain.usecase.ResetPasswordWithPhoneUseCase
import com.example.moneytracker.domain.usecase.SaveBudgetLimitUseCase
import com.example.moneytracker.domain.usecase.SaveSavingGoalUseCase
import com.example.moneytracker.domain.usecase.SendPasswordResetEmailUseCase
import com.example.moneytracker.domain.usecase.SendPhoneOtpUseCase
import com.example.moneytracker.domain.usecase.SetCurrencyUseCase
import com.example.moneytracker.domain.usecase.SetHighValueProtectionEnabledUseCase
import com.example.moneytracker.domain.usecase.SetLanguageUseCase
import com.example.moneytracker.domain.usecase.SetNotificationsEnabledUseCase
import com.example.moneytracker.domain.usecase.SetThemeUseCase
import com.example.moneytracker.domain.usecase.UpdatePasswordUseCase
import com.example.moneytracker.domain.usecase.UpdateProfileUseCase
import com.example.moneytracker.domain.usecase.VerifyPasswordResetCodeUseCase
import com.example.moneytracker.domain.usecase.VerifyPhoneOtpUseCase
import com.example.moneytracker.presentation.auth.NewPasswordViewModel
import com.example.moneytracker.presentation.auth.OtpVerificationViewModel

object AppContainer {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // ── Data Sources ─────────────────────────────────────────────────────

    private val localTransactionDataSource by lazy {
        SharedPreferencesTransactionLocalDataSource(appContext)
    }

    private val remoteTransactionDataSource = FirebaseTransactionRemoteDataSource()

    // ── Repositories ─────────────────────────────────────────────────────

    private val sharedPrefsManager: SharedPrefsManager by lazy {
        SharedPrefsManager(appContext)
    }

    private val transactionRepository: TransactionRepository by lazy {
        TransactionRepositoryImp(
            remoteDataSource = remoteTransactionDataSource,
            localDataSource = localTransactionDataSource
        )
    }

    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository()
    }

    private val settingsRepository: SettingsRepository by lazy {
        SharedPreferencesSettingsRepository(sharedPrefsManager)
    }

    private val exchangeRateRepository: ExchangeRateRepository by lazy {
        OpenExchangeRateRepository(sharedPrefsManager)
    }

    val profileRepository: ProfileRepository by lazy {
        SharedPreferencesProfileRepository(sharedPrefsManager)
    }

    private val securitySettingsRepository: SecuritySettingsRepository by lazy {
        SharedPreferencesSecuritySettingsRepository(sharedPrefsManager)
    }

    private val exportReportRepository: ExportReportRepository by lazy {
        InternalStorageExportReportRepository(appContext)
    }

    private val budgetRepository: BudgetRepository by lazy {
        SharedPreferencesBudgetRepository(appContext)
    }

    // ── Transaction Use Cases ─────────────────────────────────────────────

    val getTransactionsUseCase: GetTransactionsUseCase by lazy {
        GetTransactionsUseCase(transactionRepository)
    }

    val addTransactionUseCase: AddTransactionUseCase by lazy {
        AddTransactionUseCase(transactionRepository)
    }

    val deleteTransactionUseCase: DeleteTransactionUseCase by lazy {
        DeleteTransactionUseCase(transactionRepository)
    }

    val getDashboardSummaryUseCase: GetDashboardSummaryUseCase by lazy {
        GetDashboardSummaryUseCase(transactionRepository)
    }

    val getSpendingReportUseCase: GetSpendingReportUseCase by lazy {
        GetSpendingReportUseCase(transactionRepository)
    }

    // ── Auth Use Cases ────────────────────────────────────────────────────

    val loginUseCase: LoginUseCase by lazy {
        LoginUseCase(authRepository)
    }

    val isUserLoggedInUseCase: IsUserLoggedInUseCase by lazy {
        IsUserLoggedInUseCase(authRepository)
    }

    val isCurrentUserGoogleAccountUseCase: IsCurrentUserGoogleAccountUseCase by lazy {
        IsCurrentUserGoogleAccountUseCase(authRepository)
    }

    val logoutUseCase: LogoutUseCase by lazy {
        LogoutUseCase(authRepository)
    }

    val deleteAccountUseCase: DeleteAccountUseCase by lazy {
        DeleteAccountUseCase(authRepository)
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

    val updatePasswordUseCase: UpdatePasswordUseCase by lazy {
        UpdatePasswordUseCase(authRepository)
    }

    val sendPhoneOtpUseCase: SendPhoneOtpUseCase by lazy {
        SendPhoneOtpUseCase(authRepository)
    }

    val verifyPhoneOtpUseCase: VerifyPhoneOtpUseCase by lazy {
        VerifyPhoneOtpUseCase(authRepository)
    }

    val resetPasswordWithPhoneUseCase: ResetPasswordWithPhoneUseCase by lazy {
        ResetPasswordWithPhoneUseCase(authRepository)
    }

    val linkPhoneUseCase: LinkPhoneUseCase by lazy {
        LinkPhoneUseCase(authRepository)
    }

    // ── Budget Use Cases ──────────────────────────────────────────────────

    val getBudgetOverviewUseCase: GetBudgetOverviewUseCase by lazy {
        GetBudgetOverviewUseCase(budgetRepository, transactionRepository)
    }

    val saveBudgetLimitUseCase: SaveBudgetLimitUseCase by lazy {
        SaveBudgetLimitUseCase(budgetRepository)
    }

    val deleteBudgetLimitUseCase: DeleteBudgetLimitUseCase by lazy {
        DeleteBudgetLimitUseCase(budgetRepository)
    }

    val saveSavingGoalUseCase: SaveSavingGoalUseCase by lazy {
        SaveSavingGoalUseCase(budgetRepository, transactionRepository)
    }

    val deleteSavingGoalUseCase: DeleteSavingGoalUseCase by lazy {
        DeleteSavingGoalUseCase(budgetRepository, transactionRepository)
    }

    // ── Settings Use Cases ────────────────────────────────────────────────

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

    val setCurrencyUseCase: SetCurrencyUseCase by lazy {
        SetCurrencyUseCase(settingsRepository)
    }

    val refreshExchangeRatesUseCase: RefreshExchangeRatesUseCase by lazy {
        RefreshExchangeRatesUseCase(exchangeRateRepository)
    }

    // ── Report Use Cases ──────────────────────────────────────────────────

    val exportReportUseCase: ExportReportUseCase by lazy {
        ExportReportUseCase(transactionRepository, exportReportRepository)
    }

    // ── Profile Use Cases ─────────────────────────────────────────────────

    val getProfileUseCase: GetProfileUseCase by lazy {
        GetProfileUseCase(profileRepository)
    }

    val updateProfileUseCase: UpdateProfileUseCase by lazy {
        UpdateProfileUseCase(profileRepository)
    }

    // ── Security Use Cases ────────────────────────────────────────────────

    val getSecuritySettingsUseCase: GetSecuritySettingsUseCase by lazy {
        GetSecuritySettingsUseCase(securitySettingsRepository)
    }

    val setHighValueProtectionEnabledUseCase: SetHighValueProtectionEnabledUseCase by lazy {
        SetHighValueProtectionEnabledUseCase(securitySettingsRepository)
    }

    // ── ViewModel Factories ───────────────────────────────────────────────

    val otpVerificationViewModelFactory by lazy {
        OtpVerificationViewModel.Factory(
            verifyPhoneOtpUseCase, sendPhoneOtpUseCase, linkPhoneUseCase
        )
    }

    val newPasswordViewModelFactory by lazy {
        NewPasswordViewModel.Factory(
            resetPasswordWithPhoneUseCase, loginUseCase
        )
    }
}
