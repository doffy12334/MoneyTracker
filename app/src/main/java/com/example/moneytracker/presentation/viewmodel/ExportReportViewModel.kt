package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.domain.model.report.ExportFileFormat
import com.example.moneytracker.domain.model.report.ExportPeriod
import com.example.moneytracker.domain.model.report.ExportReportRequest
import com.example.moneytracker.domain.usecase.ExportReportUseCase
import com.example.moneytracker.presentation.uistate.ExportReportUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExportReportViewModel(
    private val exportReportUseCase: ExportReportUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExportReportUiState())
    val uiState: StateFlow<ExportReportUiState> = _uiState.asStateFlow()

    fun selectPeriod(period: ExportPeriod) {
        _uiState.update {
            it.copy(
                selectedPeriod = period,
                exportedReport = null,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun selectFormat(format: ExportFileFormat) {
        _uiState.update {
            it.copy(
                selectedFormat = format,
                exportedReport = null,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun selectCustomDateRange(startDate: String, endDate: String) {
        _uiState.update {
            it.copy(
                selectedPeriod = ExportPeriod.CUSTOM,
                customStartDate = startDate,
                customEndDate = endDate,
                exportedReport = null,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun exportReport() {
        val state = _uiState.value
        if (state.selectedPeriod == ExportPeriod.CUSTOM &&
            (state.customStartDate.isNullOrBlank() || state.customEndDate.isNullOrBlank())
        ) {
            _uiState.update { it.copy(errorMessage = "CUSTOM_DATE_REQUIRED") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, successMessage = null, errorMessage = null) }
            runCatching {
                exportReportUseCase(
                    ExportReportRequest(
                        period = state.selectedPeriod,
                        fileFormat = state.selectedFormat,
                        customStartDate = state.customStartDate,
                        customEndDate = state.customEndDate
                    )
                )
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportedReport = result,
                        successMessage = null
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        errorMessage = exception.message.orEmpty()
                    )
                }
            }
        }
    }

    fun consumeExportedReport() {
        _uiState.update { it.copy(exportedReport = null) }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    class Factory(
        private val exportReportUseCase: ExportReportUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExportReportViewModel::class.java)) {
                return ExportReportViewModel(exportReportUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
