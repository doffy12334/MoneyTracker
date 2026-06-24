package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.budget.BudgetLimit
import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.repository.BudgetRepository

class SaveBudgetLimitUseCase(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(category: TransactionCategory, amount: Double) {
        require(amount > 0.0) { "Budget limit must be greater than 0" }
        budgetRepository.saveBudgetLimit(BudgetLimit(category, amount))
    }
}
