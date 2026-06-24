package com.example.moneytracker.domain.model.report


data class ReportCategoryBreakdown(
    val categoryName: String,
    val amount: Double,
    val percent: Int
)

data class SpendingReportSummary(
    val totalSpent: Double,
    val breakdown: List<ReportCategoryBreakdown>
)
