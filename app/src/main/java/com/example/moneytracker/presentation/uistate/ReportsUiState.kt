package com.example.moneytracker.presentation.uistate

import com.example.moneytracker.domain.model.report.ReportCategoryBreakdown
import com.example.moneytracker.domain.model.report.ReportPeriod

data class ReportsUiState(
    val selectedPeriod: ReportPeriod = ReportPeriod.MONTHLY,
    val totalSpent: Double = 0.0,
    val breakdown: List<ReportCategoryBreakdown> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
