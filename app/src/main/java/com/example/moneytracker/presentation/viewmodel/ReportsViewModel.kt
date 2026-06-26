package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.domain.model.report.ReportPeriod
import com.example.moneytracker.domain.usecase.GetSpendingReportUseCase
import com.example.moneytracker.presentation.uistate.ReportsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReportsViewModel(
    private val getSpendingReportUseCase: GetSpendingReportUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadReport()
    }

    fun onPeriodSelected(period: ReportPeriod) {
        if (_uiState.value.selectedPeriod == period) return
        _uiState.update { it.copy(selectedPeriod = period, errorMessage = null) }
        loadReport()
    }

    fun loadReport() {
        viewModelScope.launch {
            val period = _uiState.value.selectedPeriod
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val summary = getSpendingReportUseCase(period)
                _uiState.update {
                    it.copy(
                        totalSpent = summary.totalSpent,
                        breakdown = summary.breakdown,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        totalSpent = 0.0,
                        breakdown = emptyList(),
                        isLoading = false,
                        errorMessage = exception.message ?: "Khong the tai bao cao"
                    )
                }
            }
        }
    }

    class Factory(
        private val getSpendingReportUseCase: GetSpendingReportUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
                return ReportsViewModel(getSpendingReportUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
