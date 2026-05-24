package com.example.moneytracker.presentation.uistate

import com.example.moneytracker.domain.model.ReportCategoryBreakdown
import com.example.moneytracker.domain.model.ReportPeriod

data class ReportsUiState(
    val selectedPeriod: ReportPeriod = ReportPeriod.MONTHLY,
    val totalSpent: Double = 0.0,
    val breakdown: List<ReportCategoryBreakdown> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
