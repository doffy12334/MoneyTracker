package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.SavingGoal
import com.example.moneytracker.domain.repository.BudgetRepository
import java.util.UUID

class SaveSavingGoalUseCase(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(
        id: String?,
        title: String,
        targetAmount: Double,
        currentAmount: Double
    ) {
        require(title.isNotBlank()) { "Saving goal name cannot be empty" }
        require(targetAmount > 0.0) { "Target amount must be greater than 0" }
        require(currentAmount >= 0.0) { "Saved amount cannot be negative" }
        budgetRepository.saveSavingGoal(
            SavingGoal(
                id = id?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
                title = title.trim(),
                targetAmount = targetAmount,
                currentAmount = currentAmount
            )
        )
    }
}
