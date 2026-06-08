package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.repository.BudgetRepository

class DeleteBudgetLimitUseCase(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(category: TransactionCategory) {
        budgetRepository.deleteBudgetLimit(category)
    }
}
