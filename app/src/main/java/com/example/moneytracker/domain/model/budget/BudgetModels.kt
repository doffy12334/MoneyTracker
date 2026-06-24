package com.example.moneytracker.domain.model.budget


import com.example.moneytracker.domain.model.transaction.TransactionCategory

data class BudgetLimit(
    val category: TransactionCategory,
    val limitAmount: Double
)

data class BudgetCategoryProgress(
    val category: TransactionCategory,
    val categoryName: String,
    val limitAmount: Double,
    val spentAmount: Double,
    val progressPercent: Int,
    val remainingAmount: Double,
    val isExceeded: Boolean
)

data class SavingGoal(
    val id: String,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double
)

data class SavingGoalProgress(
    val goal: SavingGoal,
    val progressPercent: Int,
    val remainingAmount: Double,
    val isCompleted: Boolean
)

data class BudgetOverview(
    val totalBudget: Double,
    val totalSpent: Double,
    val categoryProgress: List<BudgetCategoryProgress>,
    val savingGoals: List<SavingGoalProgress>
)
