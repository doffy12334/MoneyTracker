package com.example.moneytracker.presentation.uistate

import com.example.moneytracker.domain.model.ExportFileFormat
import com.example.moneytracker.domain.model.ExportPeriod
import com.example.moneytracker.domain.model.ExportReportResult

data class ExportReportUiState(
    val selectedPeriod: ExportPeriod = ExportPeriod.CURRENT_MONTH,
    val selectedFormat: ExportFileFormat = ExportFileFormat.CSV,
    val customStartDate: String? = null,
    val customEndDate: String? = null,
    val isExporting: Boolean = false,
    val exportedReport: ExportReportResult? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
