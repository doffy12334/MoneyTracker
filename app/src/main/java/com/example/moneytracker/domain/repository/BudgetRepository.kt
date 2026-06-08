package com.example.moneytracker.domain.repository

import com.example.moneytracker.domain.model.BudgetLimit
import com.example.moneytracker.domain.model.SavingGoal
import com.example.moneytracker.domain.model.transaction.TransactionCategory

interface BudgetRepository {
    suspend fun getBudgetLimits(): List<BudgetLimit>
    suspend fun saveBudgetLimit(limit: BudgetLimit)
    suspend fun deleteBudgetLimit(category: TransactionCategory)
    suspend fun getSavingGoals(): List<SavingGoal>
    suspend fun saveSavingGoal(goal: SavingGoal)
    suspend fun deleteSavingGoal(goalId: String)
}
