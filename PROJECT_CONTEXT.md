# MoneyTracker Project Context

Read this file first when continuing the project in a new chat. It summarizes the current architecture, implemented flows, important files, and expected direction.

## Project Overview

MoneyTracker is an Android Kotlin app using XML layouts, ViewBinding, Navigation Component, Firebase Auth, Google Sign-In dependencies, MPAndroidChart, SharedPreferences, and a manual dependency container.

Target architecture:

```text
presentation -> domain -> data
```

Rules to preserve:
- Fragment handles UI events, rendering state, navigation, and small Android UI concerns only.
- ViewModel owns screen state and calls use cases.
- Use cases contain business rules and validation.
- Repository interfaces live in `domain/repository`.
- Repository implementations and data sources live in `data`.
- Do not call Firebase, SharedPreferences, file storage, or Android storage APIs directly from Fragment/ViewModel.

## Build Info

Main module:

```text
app
```

Useful command:

```powershell
.\gradlew.bat assembleDebug
```

Gradle may need permission outside sandbox because it accesses:

```text
C:\Users\phuc\.gradle\wrapper\dists
```

Firebase config is present:

```text
app/google-services.json
```

## Current Package Structure

```text
data/
  local/
  FakeTransactionRemoteDataSource.kt
  FirebaseAuthRepository.kt
  InMemoryTransactionLocalDataSource.kt
  InternalStorageExportReportRepository.kt
  SharedPreferencesProfileRepository.kt
  SharedPreferencesSecuritySettingsRepository.kt
  SharedPreferencesSettingsRepository.kt
  TransactionRepositoryImp.kt

domain/
  model/
  repository/
  usecase/

presentation/
  adapter/
  ui/
    about/
    activities/
    addtransaction/
    auth/
    budget/
    dashboard/
    history/
    onboarding/
    profile/
    reports/
    security/
    settings/
    views/
  uistate/
  viewmodel/
```

Important: do not reintroduce `presentation/ui/fragments`. Auth and onboarding fragments now live in feature packages.

## Dependency Container

File:

```text
app/src/main/java/com/example/moneytracker/di/AppContainer.kt
```

`AppContainer` manually wires repositories and use cases. It is initialized from:

```text
app/src/main/java/com/example/moneytracker/MoneyTrackerApplication.kt
```

Register new use cases/repositories in `AppContainer`.

## Navigation

Navigation file:

```text
app/src/main/res/navigation/nav_graph.xml
```

Important destinations:

```text
onboardingFragment
loginFragment
registerFragment
inputEmailFragment
dashboardFragment
historyFragment
reportsFragment
settingsFragment
profileFragment
securityCenterFragment
exportReportFragment
aboutAppFragment
budgetFragment
addTransactionFragment
```

Current fragment class paths:

```text
presentation/ui/onboarding/OnBoardingFragment.kt
presentation/ui/auth/LoginFragment.kt
presentation/ui/auth/RegisterFragment.kt
presentation/ui/auth/InputEmailFragment.kt
presentation/ui/dashboard/DashBoardFragment.kt
presentation/ui/history/HistoryFragment.kt
presentation/ui/reports/ReportsFragment.kt
presentation/ui/reports/ExportReportFragment.kt
presentation/ui/settings/SettingFragment.kt
presentation/ui/profile/ProfileFragment.kt
presentation/ui/security/SecurityCenterFragment.kt
presentation/ui/about/AboutAppFragment.kt
```

Do not reference removed or old destinations/classes:

```text
presentation.ui.fragments.LoginFragment
presentation.ui.fragments.RegisterFragment
presentation.ui.fragments.InputEmailFragment
presentation.ui.fragments.OnBoardingFragment
OtpFragment
OtpViewModel
fragment_otp
otpFragment
```

## Auth

Firebase Auth is wired through Clean Architecture.

Domain:

```text
domain/repository/AuthRepository.kt
domain/usecase/LoginUseCase.kt
domain/usecase/LoginWithGoogleUseCase.kt
domain/usecase/RegisterUseCase.kt
domain/usecase/SendPasswordResetEmailUseCase.kt
domain/usecase/VerifyPasswordResetCodeUseCase.kt
domain/usecase/IsUserLoggedInUseCase.kt
domain/usecase/LogoutUseCase.kt
```

Data:

```text
data/FirebaseAuthRepository.kt
```

Presentation:

```text
presentation/ui/auth/LoginFragment.kt
presentation/ui/auth/RegisterFragment.kt
presentation/ui/auth/InputEmailFragment.kt
presentation/viewmodel/LoginViewModel.kt
presentation/viewmodel/RegisterViewModel.kt
presentation/viewmodel/InputEmailViewModel.kt
presentation/uistate/LoginUiState.kt
presentation/uistate/RegisterUiState.kt
presentation/uistate/InputEmailUiState.kt
```

Current auth flow:

```text
Onboarding -> Login
Login -> Register
Login -> InputEmail
InputEmail: enter email -> Confirm -> Firebase sends reset password email -> pop back to Login
Login success -> Dashboard
Register success -> Dashboard
```

Firebase password reset sends a reset link, not a 6-digit OTP.

Login currently includes the custom pig password animation through:

```text
presentation/ui/views/PigLoginView.kt
res/layout/fragment_login.xml
```

## Transactions

Current transaction data is not persisted after app restart.

Domain:

```text
domain/model/DashboardSummary.kt
domain/model/transaction/Transaction.kt
domain/model/transaction/TransactionType.kt
domain/model/transaction/Category.kt
domain/repository/TransactionRepository.kt
domain/usecase/AddTransactionUseCase.kt
domain/usecase/GetTransactionsUseCase.kt
domain/usecase/GetDashboardSummaryUseCase.kt
domain/usecase/GetSpendingReportUseCase.kt
```

Data:

```text
data/FakeTransactionRemoteDataSource.kt
data/InMemoryTransactionLocalDataSource.kt
data/TransactionLocalDataSource.kt
data/TransactionRemoteDataSource.kt
data/TransactionRepositoryImp.kt
```

Current behavior:
- `FakeTransactionRemoteDataSource` provides sample remote-like data.
- `InMemoryTransactionLocalDataSource` caches only while the app process is alive.
- Transactions are not saved to Firestore or Room at this point.
- Dashboard, History, Reports, and Export all read through `TransactionRepository`.

Current flow:

```text
Dashboard -> Add Transaction -> Save -> navigate back -> Dashboard refreshes
History -> loads transactions -> supports text search by name/category
Reports -> groups expense transactions by category
Export -> exports current repository data to internal app files
```

## Reports And Export

Reports now has a donut chart using MPAndroidChart and a category breakdown list.

Domain/data:

```text
domain/model/ReportCategoryBreakdown.kt
domain/model/ExportReportRequest.kt
domain/repository/ExportReportRepository.kt
domain/usecase/GetSpendingReportUseCase.kt
domain/usecase/ExportReportUseCase.kt
data/InternalStorageExportReportRepository.kt
```

Presentation:

```text
presentation/ui/reports/ReportsFragment.kt
presentation/ui/reports/ExportReportFragment.kt
presentation/viewmodel/ReportsViewModel.kt
presentation/viewmodel/ExportReportViewModel.kt
presentation/uistate/ReportsUiState.kt
presentation/uistate/ExportReportUiState.kt
presentation/adapter/ReportCategoryAdapter.kt
res/layout/fragment_reports.xml
res/layout/fragment_export_report.xml
res/layout/item_report_category.xml
```

Export creates CSV/PDF files in internal app storage. It does not yet expose share/open file UX.

## Settings

Settings follows MVVM + Clean Architecture and stores preferences in SharedPreferences.

Domain:

```text
domain/model/AppSettings.kt
domain/model/AppLanguage.kt
domain/model/AppTheme.kt
domain/repository/SettingsRepository.kt
domain/usecase/GetSettingsUseCase.kt
domain/usecase/SetNotificationsEnabledUseCase.kt
domain/usecase/SetLanguageUseCase.kt
domain/usecase/SetThemeUseCase.kt
domain/usecase/LogoutUseCase.kt
```

Data:

```text
data/local/SharedPrefsManager.kt
data/SharedPreferencesSettingsRepository.kt
```

Presentation:

```text
presentation/ui/settings/SettingFragment.kt
presentation/viewmodel/SettingsViewModel.kt
presentation/uistate/SettingsUiState.kt
res/layout/fragment_setting.xml
```

Working settings:
- notification switch persists on/off
- language selection dialog: Vietnamese/English
- theme selection dialog: Light/Dark
- theme applied via `AppCompatDelegate.setDefaultNightMode`
- language applied via `AppCompatDelegate.setApplicationLocales`
- logout is handled through `LogoutUseCase`
- rows navigate to Profile, Security, Export Report, and About screens

Because several setting rows are included via `<include>`, `SettingFragment` uses `binding.root.findViewById(...)` for included child IDs.

## Profile

Profile owns personal information only. Do not put 2FA/security controls into Profile.

Domain/data:

```text
domain/model/UserProfile.kt
domain/repository/ProfileRepository.kt
domain/usecase/GetProfileUseCase.kt
domain/usecase/UpdateProfileUseCase.kt
data/SharedPreferencesProfileRepository.kt
```

Presentation:

```text
presentation/ui/profile/ProfileFragment.kt
presentation/viewmodel/ProfileViewModel.kt
presentation/uistate/ProfileUiState.kt
res/layout/fragment_profile.xml
```

## Security

Security owns account protection features.

Domain/data:

```text
domain/model/SecuritySettings.kt
domain/repository/SecuritySettingsRepository.kt
domain/usecase/GetSecuritySettingsUseCase.kt
domain/usecase/SetTwoFactorEnabledUseCase.kt
domain/usecase/SetBiometricEnabledUseCase.kt
domain/usecase/SetHighValueProtectionEnabledUseCase.kt
data/SharedPreferencesSecuritySettingsRepository.kt
```

Presentation:

```text
presentation/ui/security/SecurityCenterFragment.kt
presentation/viewmodel/SecurityCenterViewModel.kt
presentation/uistate/SecurityCenterUiState.kt
res/layout/fragment_security_center.xml
```

Security currently includes:
- change password via Firebase reset email
- 2FA preference toggle
- biometric preference toggle
- high-value protection preference toggle
- static login history UI

## About

Presentation:

```text
presentation/ui/about/AboutAppFragment.kt
res/layout/fragment_about_app.xml
```

## Important Layouts

```text
res/layout/activity_main.xml
res/layout/fragment_on_boarding.xml
res/layout/fragment_login.xml
res/layout/fragment_register.xml
res/layout/fragment_input_email.xml
res/layout/fragment_dash_board.xml
res/layout/fragment_add_transaction.xml
res/layout/fragment_history.xml
res/layout/fragment_reports.xml
res/layout/fragment_export_report.xml
res/layout/fragment_setting.xml
res/layout/fragment_profile.xml
res/layout/fragment_security_center.xml
res/layout/fragment_about_app.xml
res/layout/fragment_budget.xml
```

Settings row layouts:

```text
item_setting_profile.xml
item_setting_security.xml
item_setting_premium.xml
item_setting_notification.xml
item_setting_language.xml
item_setting_theme.xml
item_export_data.xml
item_delete_account.xml
item_about_app.xml
item_terms.xml
```

## Known Issues / Technical Debt

1. Transactions are not persisted.
   - Current data uses fake/in-memory sources.
   - Add Room or Firestore when persistence is required.

2. Some visible strings are hardcoded in XML/Kotlin.
   - Move to `strings.xml` when polishing localization.

3. Some Vietnamese text in older XML may have encoding artifacts.
   - Clean gradually when touching those layouts.

4. Budget screen is still mostly a placeholder.
   - Needs ViewModel/use cases/data once budget feature is implemented.

5. Manual DI is used.
   - Acceptable for now.
   - Hilt can replace `AppContainer` later.

## Recommended Next Work Order

1. Add persistent transaction storage.
   - Firestore if cloud sync across devices is required.
   - Room if offline-first local persistence is required.

2. Improve AddTransaction.
   - Date picker
   - Category UI
   - Better validation
   - Currency formatting

3. Improve History.
   - filter by income/expense
   - filter by category/date/month
   - transaction detail screen
   - delete/edit transaction

4. Improve Dashboard.
   - monthly income
   - monthly expense
   - real balance from persisted data
   - recent transactions

5. Improve Reports and Export.
   - monthly income/expense chart
   - file sharing/opening for exported CSV/PDF
   - better empty states

6. Improve Budget.
   - budget model/entity
   - monthly budget by category
   - progress and warning

## Coding Conventions To Keep

When adding a new feature, create:

```text
domain/model/...
domain/repository/...
domain/usecase/...
data/...
presentation/uistate/FeatureUiState.kt
presentation/viewmodel/FeatureViewModel.kt
presentation/ui/<feature>/FeatureFragment.kt
res/layout/fragment_feature.xml
```

Preferred ViewModel pattern:

```kotlin
class FeatureViewModel(
    private val useCase: SomeUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeatureUiState())
    val uiState = _uiState.asStateFlow()
}
```

Preferred Fragment pattern:

```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect(::renderState)
    }
}
```

Avoid:
- repository implementation in `domain`
- Android SDK imports in domain use cases
- Firebase/SharedPreferences/file storage calls inside Fragment or ViewModel
- adding empty placeholder classes
- adding unused navigation destinations
- reintroducing removed OTP flow without backend support
- reintroducing `presentation/ui/fragments`

## Last Known Build Status

The project built successfully after fixing navigation package references:

```powershell
.\gradlew.bat assembleDebug
```

Result:

```text
BUILD SUCCESSFUL
```
