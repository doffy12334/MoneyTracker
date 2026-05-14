package com.example.moneytracker.presentation.uistate

import com.example.moneytracker.domain.model.transaction.Transaction

data class HistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
