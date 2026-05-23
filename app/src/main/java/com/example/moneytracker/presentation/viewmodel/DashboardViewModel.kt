package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.domain.usecase.GetDashboardSummaryUseCase
import com.example.moneytracker.presentation.uistate.DashboardUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private var showAllTransactions = false

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            _uiState.value = try {
                val summary = getDashboardSummaryUseCase()
                DashboardUiState.Success(
                    transactions = if (showAllTransactions) {
                        summary.transactions
                    } else {
                        summary.transactions.take(RECENT_TRANSACTION_LIMIT)
                    },
                    totalBalance = summary.totalBalance,
                    totalIncome = summary.totalIncome,
                    totalExpense = summary.totalExpense,
                    balanceRate = summary.balanceRate,
                    showAllTransactions = showAllTransactions
                )
            } catch (exception: Exception) {
                DashboardUiState.Error(
                    message = exception.message ?: "Khong the tai tong quan tai chinh"
                )
            }
        }
    }

    fun toggleShowAllTransactions() {
        showAllTransactions = !showAllTransactions
        loadDashboard()
    }

    class Factory(
        private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                return DashboardViewModel(getDashboardSummaryUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    private companion object {
        const val RECENT_TRANSACTION_LIMIT = 5
    }
}
