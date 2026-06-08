package com.example.moneytracker.presentation.uistate

import com.example.moneytracker.domain.model.BudgetOverview

sealed interface BudgetUiState {
    data object Loading : BudgetUiState
    data class Success(val overview: BudgetOverview) : BudgetUiState
    data class Error(val message: String) : BudgetUiState
}
