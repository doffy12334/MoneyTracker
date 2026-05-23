package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.ReportCategoryBreakdown
import com.example.moneytracker.domain.model.SpendingReportSummary
import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.model.transaction.TransactionType
import com.example.moneytracker.domain.repository.TransactionRepository
import kotlin.math.roundToInt

class GetSpendingReportUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(): SpendingReportSummary {
        val expenses = transactionRepository.getTransactions()
            .filter { it.type == TransactionType.EXPENSE }

        val totalSpent = expenses.sumOf { it.amount }
        val breakdown = expenses
            .groupBy { it.category }
            .map { (category, transactions) ->
                val amount = transactions.sumOf { it.amount }
                ReportCategoryBreakdown(
                    categoryName = category.displayName(),
                    amount = amount,
                    percent = if (totalSpent > 0.0) ((amount / totalSpent) * 100).roundToInt() else 0
                )
            }
            .sortedByDescending { it.amount }

        return SpendingReportSummary(
            totalSpent = totalSpent,
            breakdown = breakdown
        )
    }

    private fun TransactionCategory.displayName(): String {
        return when (this) {
            TransactionCategory.FOOD -> "Food"
            TransactionCategory.TRANSPORT -> "Transport"
            TransactionCategory.SHOPPING -> "Shopping"
            TransactionCategory.SALARY -> "Salary"
            TransactionCategory.ENTERTAINMENT -> "Entertainment"
            TransactionCategory.OTHER -> "Other"
        }
    }
}
