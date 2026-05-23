package com.example.moneytracker.presentation.uistate

import com.example.moneytracker.domain.model.transaction.Transaction

sealed class DashboardUiState {
    object Loading : DashboardUiState()

    data class Success(
        val transactions: List<Transaction>,
        val totalBalance: Double,
        val totalIncome: Double,
        val totalExpense: Double,
        val balanceRate: Double,
        val showAllTransactions: Boolean
    ) : DashboardUiState()

    data class Error(
        val message: String
    ) : DashboardUiState()
}
