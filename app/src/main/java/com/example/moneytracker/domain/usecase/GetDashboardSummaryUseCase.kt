package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.DashboardSummary
import com.example.moneytracker.domain.model.transaction.TransactionType
import com.example.moneytracker.domain.repository.TransactionRepository

class GetDashboardSummaryUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(): DashboardSummary {
        val transactions = transactionRepository.getTransactions()
        val income = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        val expense = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        return DashboardSummary(
            totalBalance = income - expense,
            totalIncome = income,
            totalExpense = expense,
            recentTransactions = transactions.take(5)
        )
    }
}
