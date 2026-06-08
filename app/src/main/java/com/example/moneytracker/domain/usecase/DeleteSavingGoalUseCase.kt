package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.repository.BudgetRepository

class DeleteSavingGoalUseCase(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(goalId: String) {
        budgetRepository.deleteSavingGoal(goalId)
    }
}
