package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.model.transaction.TransactionType
import com.example.moneytracker.domain.repository.BudgetRepository
import com.example.moneytracker.domain.repository.TransactionRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import java.util.UUID

class DeleteSavingGoalUseCase(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(goalId: String) {
        val oldGoal = budgetRepository.getSavingGoals().find { it.id == goalId }
        budgetRepository.deleteSavingGoal(goalId)
        
        if (oldGoal != null && oldGoal.currentAmount > 0.0) {
            val dateString = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date())
            transactionRepository.addTransaction(
                com.example.moneytracker.domain.model.transaction.Transaction(
                    id = UUID.randomUUID().toString(),
                    name = "Hủy tiết kiệm: ${oldGoal.title}",
                    amount = oldGoal.currentAmount,
                    date = dateString,
                    category = TransactionCategory.OTHER,
                    type = TransactionType.INCOME
                )
            )
        }
    }
}
