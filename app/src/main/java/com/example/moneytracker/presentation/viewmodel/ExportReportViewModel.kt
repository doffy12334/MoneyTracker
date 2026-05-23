package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.domain.model.ExportFileFormat
import com.example.moneytracker.domain.model.ExportPeriod
import com.example.moneytracker.domain.model.ExportReportRequest
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
        _uiState.update { it.copy(selectedPeriod = period, successMessage = null, errorMessage = null) }
    }

    fun selectFormat(format: ExportFileFormat) {
        _uiState.update { it.copy(selectedFormat = format, successMessage = null, errorMessage = null) }
    }

    fun exportReport() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, successMessage = null, errorMessage = null) }
            runCatching {
                exportReportUseCase(
                    ExportReportRequest(
                        period = state.selectedPeriod,
                        fileFormat = state.selectedFormat
                    )
                )
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        successMessage = "Da tao ${result.fileName} voi ${result.transactionCount} giao dich"
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        errorMessage = exception.message ?: "Khong the xuat bao cao"
                    )
                }
            }
        }
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
