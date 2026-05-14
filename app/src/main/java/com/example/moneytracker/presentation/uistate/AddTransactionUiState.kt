package com.example.moneytracker.presentation.uistate

import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.model.transaction.TransactionType

data class AddTransactionUiState(
    val amount: String = "",
    val name: String = "",
    val date: String = "",
    val selectedCategory: TransactionCategory = TransactionCategory.OTHER,
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)
