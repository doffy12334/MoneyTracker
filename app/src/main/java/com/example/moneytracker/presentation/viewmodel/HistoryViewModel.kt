package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.domain.model.transaction.Transaction
import com.example.moneytracker.domain.model.transaction.TransactionType
import com.example.moneytracker.domain.usecase.DeleteTransactionUseCase
import com.example.moneytracker.domain.usecase.GetTransactionsUseCase
import com.example.moneytracker.presentation.uistate.HistoryDateFilter
import com.example.moneytracker.presentation.uistate.HistoryTypeFilter
import com.example.moneytracker.presentation.uistate.HistoryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HistoryViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
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
                applyFilters()
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
        _uiState.update { it.copy(searchQuery = query, successMessage = null, errorMessage = null) }
        applyFilters()
    }

    fun onDateFilterChanged(filter: HistoryDateFilter) {
        _uiState.update { it.copy(dateFilter = filter, successMessage = null, errorMessage = null) }
        applyFilters()
    }

    fun onTypeFilterChanged(filter: HistoryTypeFilter) {
        _uiState.update { it.copy(typeFilter = filter, successMessage = null, errorMessage = null) }
        applyFilters()
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                deleteTransactionUseCase(transaction.id)
                allTransactions = allTransactions.filterNot { it.id == transaction.id }
                _uiState.update { it.copy(successMessage = "Da xoa giao dich", errorMessage = null) }
                applyFilters()
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = exception.message ?: "Khong the xoa giao dich",
                        successMessage = null
                    )
                }
                applyFilters()
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun applyFilters() {
        val state = _uiState.value
        val normalizedQuery = state.searchQuery.trim()
        val typeFilteredTransactions = allTransactions
            .filter { transaction -> transaction.matchesTypeFilter(state.typeFilter) }
        val periodAnchor = typeFilteredTransactions.periodAnchorCalendar()
        val filteredTransactions = typeFilteredTransactions
            .filter { transaction -> transaction.matchesDateFilter(state.dateFilter, periodAnchor) }
            .filter { transaction ->
                normalizedQuery.isBlank() ||
                    transaction.name.contains(normalizedQuery, ignoreCase = true) ||
                    transaction.category.name.contains(normalizedQuery, ignoreCase = true) ||
                    transaction.type.name.contains(normalizedQuery, ignoreCase = true)
            }
            .sortedWith(
                compareByDescending<Transaction> { it.parseHistoryDate()?.time ?: 0L }
                    .thenByDescending { it.parseCreatedAtDate()?.time ?: 0L }
            )

        _uiState.update {
            it.copy(
                transactions = filteredTransactions,
                isLoading = false,
                errorMessage = it.errorMessage
            )
        }
    }

    private fun Transaction.matchesTypeFilter(filter: HistoryTypeFilter): Boolean {
        return when (filter) {
            HistoryTypeFilter.ALL -> true
            HistoryTypeFilter.INCOME -> type == TransactionType.INCOME
            HistoryTypeFilter.EXPENSE -> type == TransactionType.EXPENSE
        }
    }

    private fun Transaction.matchesDateFilter(filter: HistoryDateFilter, periodAnchor: Calendar?): Boolean {
        val date = parseHistoryDate() ?: return false
        val transactionCalendar = Calendar.getInstance().apply { time = date }
        val anchorCalendar = periodAnchor ?: Calendar.getInstance()
        return when (filter) {
            HistoryDateFilter.WEEKLY -> transactionCalendar.isInSameWeek(anchorCalendar)
            HistoryDateFilter.MONTHLY ->
                transactionCalendar.get(Calendar.YEAR) == anchorCalendar.get(Calendar.YEAR) &&
                    transactionCalendar.get(Calendar.MONTH) == anchorCalendar.get(Calendar.MONTH)
            HistoryDateFilter.YEARLY ->
                transactionCalendar.get(Calendar.YEAR) == anchorCalendar.get(Calendar.YEAR)
        }
    }

    private fun List<Transaction>.periodAnchorCalendar(): Calendar {
        val today = Calendar.getInstance()
        val endOfToday = today.copyAtEndOfDay()
        val latestAvailableDate = mapNotNull { it.parseHistoryDate() }
            .filter { !it.after(endOfToday.time) }
            .maxByOrNull { it.time }
        return Calendar.getInstance().apply { time = latestAvailableDate ?: today.time }
    }

    private fun Transaction.parseHistoryDate(): Date? {
        return parseTransactionDate(date)
            ?: parseCreatedAtDate().takeIf { createdAt.isNotBlank() }
    }

    private fun Transaction.parseCreatedAtDate(): Date? {
        return parseTransactionDate(createdAt)
    }

    private fun Calendar.isInSameWeek(anchorCalendar: Calendar): Boolean {
        firstDayOfWeek = Calendar.MONDAY
        minimalDaysInFirstWeek = 4
        val normalizedAnchor = (anchorCalendar.clone() as Calendar).apply {
            firstDayOfWeek = Calendar.MONDAY
            minimalDaysInFirstWeek = 4
        }
        return getWeekYear() == normalizedAnchor.getWeekYear() &&
            get(Calendar.WEEK_OF_YEAR) == normalizedAnchor.get(Calendar.WEEK_OF_YEAR)
    }

    private fun Calendar.copyAtEndOfDay(): Calendar {
        return (clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
    }

    private fun parseTransactionDate(value: String): Date? {
        val trimmedValue = value.trim()
        return DATE_PATTERNS.firstNotNullOfOrNull { pattern ->
            DATE_LOCALES.firstNotNullOfOrNull { locale ->
                runCatching {
                    SimpleDateFormat(pattern, locale).apply {
                        isLenient = false
                    }.parse(trimmedValue)
                }.getOrNull()
            }
        }
    }

    class Factory(
        private val getTransactionsUseCase: GetTransactionsUseCase,
        private val deleteTransactionUseCase: DeleteTransactionUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                return HistoryViewModel(getTransactionsUseCase, deleteTransactionUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    private companion object {
        val DATE_LOCALES = listOf(Locale.US, Locale.ENGLISH, Locale.forLanguageTag("vi-VN"))
        val DATE_PATTERNS = listOf(
            "dd MMM yyyy",
            "d MMM yyyy",
            "dd MMMM yyyy",
            "d MMMM yyyy",
            "dd/MM/yyyy",
            "d/M/yyyy",
            "dd-MM-yyyy",
            "d-M-yyyy",
            "yyyy-MM-dd",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "EEE MMM dd HH:mm:ss zzz yyyy"
        )
    }
}
