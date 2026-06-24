package com.example.moneytracker.domain.usecase

import com.example.moneytracker.R
import com.example.moneytracker.domain.exception.AppException
import com.example.moneytracker.domain.model.budget.SavingGoal
import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.model.transaction.TransactionType
import com.example.moneytracker.domain.repository.BudgetRepository
import com.example.moneytracker.domain.repository.TransactionRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SaveSavingGoalUseCase(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        id: String?,
        title: String,
        targetAmount: Double,
        currentAmount: Double
    ) {
        if (title.isBlank()) throw AppException(R.string.error_empty_saving_goal_name)
        if (targetAmount <= 0.0) throw AppException(R.string.error_saving_goal_target_positive)
        if (currentAmount < 0.0) throw AppException(R.string.error_saving_goal_saved_negative)

        val actualId = id?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        val oldGoal = budgetRepository.getSavingGoals().find { it.id == actualId }
        val difference = currentAmount - (oldGoal?.currentAmount ?: 0.0)

        budgetRepository.saveSavingGoal(
            SavingGoal(
                id = actualId,
                title = title.trim(),
                targetAmount = targetAmount,
                currentAmount = currentAmount
            )
        )

        if (difference != 0.0) {
            val dateString = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date())
            if (difference > 0) {
                transactionRepository.addTransaction(
                    com.example.moneytracker.domain.model.transaction.Transaction(
                        id = UUID.randomUUID().toString(),
                        name = "Gửi tiết kiệm: $title",
                        amount = difference,
                        date = dateString,
                        category = TransactionCategory.OTHER,
                        type = TransactionType.EXPENSE
                    )
                )
            } else {
                transactionRepository.addTransaction(
                    com.example.moneytracker.domain.model.transaction.Transaction(
                        id = UUID.randomUUID().toString(),
                        name = "Rút tiết kiệm: $title",
                        amount = -difference,
                        date = dateString,
                        category = TransactionCategory.OTHER,
                        type = TransactionType.INCOME
                    )
                )
            }
        }
    }
}
