package com.example.moneytracker.presentation.uistate

import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.model.transaction.TransactionType

data class AddTransactionUiState(
    val amount: String = "",
    val name: String = "",
    val date: String = "",
    val selectedCategory: TransactionCategory? = null,
    val selectedType: TransactionType? = null,
    val customCategory: String = "",
    val isSaving: Boolean = false,
    val errorMessage: Int? = null,
    val isSaved: Boolean = false
)
