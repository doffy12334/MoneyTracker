package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.BudgetCategoryProgress
import com.example.moneytracker.domain.model.BudgetOverview
import com.example.moneytracker.domain.model.SavingGoalProgress
import com.example.moneytracker.domain.model.transaction.Transaction
import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.model.transaction.TransactionType
import com.example.moneytracker.domain.model.transaction.isInCurrentMonth
import com.example.moneytracker.domain.repository.BudgetRepository
import com.example.moneytracker.domain.repository.TransactionRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class GetBudgetOverviewUseCase(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(): BudgetOverview {
        val limits = budgetRepository.getBudgetLimits()
        val expensesThisMonth = transactionRepository.getTransactions()
            .filter { it.type == TransactionType.EXPENSE && it.isInCurrentMonth() }
        val spentByCategory = expensesThisMonth.groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }

        val categoryProgress = limits.map { limit ->
            val spent = spentByCategory[limit.category] ?: 0.0
            val percent = if (limit.limitAmount > 0.0) {
                ((spent / limit.limitAmount) * 100).roundToInt()
            } else {
                0
            }
            BudgetCategoryProgress(
                category = limit.category,
                categoryName = limit.category.displayName(),
                limitAmount = limit.limitAmount,
                spentAmount = spent,
                progressPercent = percent.coerceAtLeast(0),
                remainingAmount = (limit.limitAmount - spent).coerceAtLeast(0.0),
                isExceeded = spent > limit.limitAmount
            )
        }.sortedByDescending { it.progressPercent }

        val savingGoals = budgetRepository.getSavingGoals().map { goal ->
            val percent = ((goal.currentAmount / goal.targetAmount) * 100).roundToInt()
            SavingGoalProgress(
                goal = goal,
                progressPercent = percent.coerceAtLeast(0),
                remainingAmount = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0),
                isCompleted = goal.currentAmount >= goal.targetAmount
            )
        }

        return BudgetOverview(
            totalBudget = limits.sumOf { it.limitAmount },
            totalSpent = categoryProgress.sumOf { it.spentAmount },
            categoryProgress = categoryProgress,
            savingGoals = savingGoals
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
