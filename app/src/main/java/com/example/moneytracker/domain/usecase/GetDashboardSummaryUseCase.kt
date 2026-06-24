package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.report.DashboardSummary
import com.example.moneytracker.domain.model.transaction.TransactionType
import com.example.moneytracker.domain.model.transaction.isInCurrentMonth
import com.example.moneytracker.domain.repository.TransactionRepository

class GetDashboardSummaryUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(): DashboardSummary {
        val transactions = transactionRepository.getTransactions()

        // Calculate total balance for ALL TIME
        val totalIncomeAllTime = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        val totalExpenseAllTime = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        val balance = totalIncomeAllTime - totalExpenseAllTime

        // Calculate income and expense for CURRENT MONTH
        val income = transactions
            .filter { it.type == TransactionType.INCOME && it.isInCurrentMonth() }
            .sumOf { it.amount }
        val expense = transactions
            .filter { it.type == TransactionType.EXPENSE && it.isInCurrentMonth() }
            .sumOf { it.amount }
        val balanceRate = if (income > 0.0) {
            (balance / income) * 100.0
        } else {
            0.0
        }

        return DashboardSummary(
            totalBalance = balance,
            totalIncome = income,
            totalExpense = expense,
            balanceRate = balanceRate,
            transactions = transactions
        )
    }
}
