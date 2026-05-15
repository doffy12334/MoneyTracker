# MoneyTracker Project Context

Use this file as the first reference when continuing work in a new chat. It summarizes the current architecture, implemented flows, important files, and expected direction.

## Project Overview

MoneyTracker is an Android Kotlin app using XML layouts, ViewBinding, Navigation Component, Firebase Auth, and a manual dependency container. The goal is to keep the code close to MVVM + Clean Architecture:

```text
presentation -> domain -> data
```

Rules to preserve:
- Fragment only handles UI events, rendering state, navigation, and small Android UI concerns.
- ViewModel owns screen state and calls use cases.
- Use cases contain business rules/validation.
- Repository interfaces live in `domain/repository`.
- Repository implementations/data sources live in `data`.
- Do not call Firebase, SharedPreferences, or storage APIs directly from Fragment/ViewModel.

## Important Build Info

Main module: `app`

Useful command:

```powershell
.\gradlew.bat assembleDebug
```

Gradle may need permission outside sandbox because it accesses:

```text
C:\Users\phuc\.gradle\wrapper\dists
```

Firebase config is already present:

```text
app/google-services.json
```

## Current Architecture

### Dependency Container

File:

```text
app/src/main/java/com/example/moneytracker/di/AppContainer.kt
```

`AppContainer` manually wires repositories and use cases. It is initialized from:

```text
app/src/main/java/com/example/moneytracker/MoneyTrackerApplication.kt
```

Manifest uses:

```xml
android:name=".MoneyTrackerApplication"
```

If adding new use cases/repositories, register them in `AppContainer`.

### Presentation Structure

```text
presentation/
  ui/
    activities/
    addtransaction/
    budget/
    dashboard/
    fragments/
    history/
    reports/
    settings/
  uistate/
  viewmodel/
  adapter/
```

Main Activity:

```text
presentation/ui/activities/MainActivity.kt
```

It hosts `NavHostFragment`, bottom navigation, and hides top/bottom bars for auth screens.

## Implemented Features

### Auth

Firebase Auth is wired through Clean Architecture.

Domain:

```text
domain/repository/AuthRepository.kt
domain/usecase/LoginUseCase.kt
domain/usecase/RegisterUseCase.kt
domain/usecase/SendPasswordResetEmailUseCase.kt
domain/usecase/VerifyPasswordResetCodeUseCase.kt
```

Data:

```text
data/FirebaseAuthRepository.kt
```

Presentation:

```text
presentation/ui/fragments/LoginFragment.kt
presentation/ui/fragments/RegisterFragment.kt
presentation/ui/fragments/InputEmailFragment.kt
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

Note: Firebase password reset sends a reset link, not a 6-digit OTP. Do not reintroduce OTP unless a backend/Cloud Function is added.

### Onboarding

Files:

```text
presentation/ui/fragments/OnBoardingFragment.kt
res/layout/fragment_on_boarding.xml
```

UI resembles the provided Financier reference:
- `Financier` title
- `Bỏ qua`
- large finance hero panel
- `+12.5%` badge
- `Tiếp tục` button

Both skip and continue navigate to Login.

### Transactions

Current transaction data is in memory, not persisted after app restart.

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
```

Data:

```text
data/InMemoryTransactionLocalDataSource.kt
data/FakeTransactionRemoteDataSource.kt
data/TransactionLocalDataSource.kt
data/TransactionRemoteDataSource.kt
data/TransactionRepositoryImp.kt
```

Presentation:

```text
presentation/ui/dashboard/DashBoardFragment.kt
presentation/ui/addtransaction/AddTransactionFragment.kt
presentation/ui/history/HistoryFragment.kt
presentation/viewmodel/DashboardViewModel.kt
presentation/viewmodel/AddTransactionViewModel.kt
presentation/viewmodel/HistoryViewModel.kt
presentation/uistate/DashboardUiState.kt
presentation/uistate/AddTransactionUiState.kt
presentation/uistate/HistoryUiState.kt
presentation/adapter/TransactionAdapter.kt
```

Current flow:

```text
Dashboard -> Add Transaction -> Save -> navigate back -> Dashboard refreshes
History -> loads transactions -> supports text search by name/category
```

Dashboard uses:

```text
GetDashboardSummaryUseCase
```

Add Transaction uses:

```text
AddTransactionUseCase
```

Next production step: replace in-memory data with Room.

### Settings

Settings now follows MVVM + Clean Architecture and stores preferences in SharedPreferences.

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
res/layout/item_setting_notification.xml
res/layout/item_setting_language.xml
res/layout/item_setting_theme.xml
```

Working settings:
- notification switch persists on/off
- language selection dialog: Vietnamese/English
- theme selection dialog: Light/Dark
- theme applied via `AppCompatDelegate.setDefaultNightMode`
- language applied via `AppCompatDelegate.setApplicationLocales`

Because several setting rows are included via `<include>`, `SettingFragment` uses `binding.root.findViewById(...)` for included child IDs.

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
budgetFragment
addTransactionFragment
```

Do not reference removed OTP destinations:

```text
OtpFragment
OtpViewModel
fragment_otp
otpFragment
```

These were removed because the current requirement is password reset by email link, then return to Login.

## UI Notes

The app uses XML layouts with ViewBinding.

Important layouts:

```text
res/layout/fragment_on_boarding.xml
res/layout/fragment_login.xml
res/layout/fragment_register.xml
res/layout/fragment_input_email.xml
res/layout/fragment_dash_board.xml
res/layout/fragment_add_transaction.xml
res/layout/fragment_history.xml
res/layout/fragment_reports.xml
res/layout/fragment_setting.xml
res/layout/fragment_budget.xml
```

Settings has several included row layouts:

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
   - Current data uses `InMemoryTransactionLocalDataSource`.
   - Add Room next.

2. Some visible strings are hardcoded in XML/Kotlin.
   - Move to `strings.xml` when polishing localization.

3. Some Vietnamese text in older XML may have encoding artifacts.
   - Clean gradually when touching those layouts.

4. Reports and Budget screens are mostly UI placeholders.
   - They need ViewModel/use cases/data once transaction persistence is added.

5. Manual DI is used.
   - It is acceptable for now.
   - Hilt can replace `AppContainer` later.

## Recommended Next Work Order

1. Add Room database.
   - `TransactionEntity`
   - `TransactionDao`
   - `MoneyTrackerDatabase`
   - mapper `TransactionEntity <-> Transaction`
   - replace `InMemoryTransactionLocalDataSource`

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

4. Dashboard with real Room data.
   - monthly income
   - monthly expense
   - actual balance
   - recent transactions

5. Reports.
   - MPAndroidChart
   - category expense report
   - monthly income/expense chart

6. Budget.
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
- Firebase/SharedPreferences calls inside Fragment or ViewModel
- adding empty placeholder classes
- adding unused navigation destinations

## Last Known Build Status

The project built successfully after implementing Settings MVVM/Clean:

```powershell
.\gradlew.bat assembleDebug
```

Result:

```text
BUILD SUCCESSFUL
```

