package com.example.moneytracker.domain.model

import com.example.moneytracker.domain.model.transaction.Transaction

data class DashboardSummary(
    val totalBalance: Double,
    val totalIncome: Double,
    val totalExpense: Double,
    val recentTransactions: List<Transaction>
)
