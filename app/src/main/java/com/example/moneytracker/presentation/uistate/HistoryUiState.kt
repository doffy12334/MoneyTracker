package com.example.moneytracker.presentation.uistate

import com.example.moneytracker.domain.model.transaction.Transaction

data class HistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val searchQuery: String = "",
    val dateFilter: HistoryDateFilter = HistoryDateFilter.WEEKLY,
    val typeFilter: HistoryTypeFilter = HistoryTypeFilter.ALL,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

enum class HistoryDateFilter {
    WEEKLY,
    MONTHLY,
    YEARLY
}

enum class HistoryTypeFilter {
    ALL,
    INCOME,
    EXPENSE
}
