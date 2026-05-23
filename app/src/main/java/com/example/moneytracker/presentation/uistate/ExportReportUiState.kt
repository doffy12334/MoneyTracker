package com.example.moneytracker.presentation.uistate

import com.example.moneytracker.domain.model.ExportFileFormat
import com.example.moneytracker.domain.model.ExportPeriod

data class ExportReportUiState(
    val selectedPeriod: ExportPeriod = ExportPeriod.CURRENT_MONTH,
    val selectedFormat: ExportFileFormat = ExportFileFormat.CSV,
    val isExporting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
