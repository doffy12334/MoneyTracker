package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.domain.model.transaction.Transaction
import com.example.moneytracker.domain.usecase.GetTransactionsUseCase
import com.example.moneytracker.presentation.uistate.HistoryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var allTransactions: List<Transaction> = emptyList()

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                allTransactions = getTransactionsUseCase()
                applySearch(_uiState.value.searchQuery)
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Khong the tai lich su giao dich"
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        applySearch(query)
    }

    private fun applySearch(query: String) {
        val normalizedQuery = query.trim()
        val filteredTransactions = if (normalizedQuery.isBlank()) {
            allTransactions
        } else {
            allTransactions.filter { transaction ->
                transaction.name.contains(normalizedQuery, ignoreCase = true) ||
                    transaction.category.name.contains(normalizedQuery, ignoreCase = true)
            }
        }

        _uiState.update {
            it.copy(
                transactions = filteredTransactions,
                searchQuery = query,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    class Factory(
        private val getTransactionsUseCase: GetTransactionsUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                return HistoryViewModel(getTransactionsUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
