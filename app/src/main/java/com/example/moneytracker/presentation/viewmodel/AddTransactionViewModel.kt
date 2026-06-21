package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.model.transaction.TransactionType
import com.example.moneytracker.domain.usecase.AddTransactionUseCase
import com.example.moneytracker.domain.usecase.GetSettingsUseCase
import com.example.moneytracker.presentation.uistate.AddTransactionUiState
import com.example.moneytracker.domain.exception.AppException
import com.example.moneytracker.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getSettingsUseCase: GetSettingsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun onAmountChanged(amount: String) {
        _uiState.update { it.copy(amount = amount, errorMessage = null, isSaved = false) }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null, isSaved = false) }
    }

    fun onDateChanged(date: String) {
        _uiState.update { it.copy(date = date, errorMessage = null, isSaved = false) }
    }

    fun onCategorySelected(category: TransactionCategory) {
        _uiState.update { it.copy(selectedCategory = category, isSaved = false) }
    }

    fun onCategoryCleared() {
        _uiState.update { it.copy(selectedCategory = null, isSaved = false) }
    }

    fun onCustomCategoryChanged(customCategory: String) {
        _uiState.update { it.copy(customCategory = customCategory, errorMessage = null, isSaved = false) }
    }

    fun onTypeSelected(type: TransactionType) {
        _uiState.update { it.copy(selectedType = type, isSaved = false) }
    }

    fun onTypeCleared() {
        _uiState.update { it.copy(selectedType = null, isSaved = false) }
    }

    fun saveTransaction() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull()

        if (amount == null) {
            _uiState.update { it.copy(errorMessage = R.string.error_amount_must_be_positive) }
            return
        }
        val selectedCategory = state.selectedCategory
        if (selectedCategory == null) {
            _uiState.update { it.copy(errorMessage = R.string.error_select_category) }
            return
        }
        val selectedType = state.selectedType
        if (selectedType == null) {
            _uiState.update { it.copy(errorMessage = R.string.error_select_type) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, isSaved = false) }
            try {
                val amountInVnd = getSettingsUseCase().currency.toVnd(amount)
                addTransactionUseCase(
                    name = state.name,
                    amount = amountInVnd,
                    date = state.date,
                    category = selectedCategory,
                    customCategory = if (selectedCategory == TransactionCategory.OTHER) state.customCategory else null,
                    type = selectedType
                )
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (exception: AppException) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = exception.messageResId
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = R.string.error_save_failed
                    )
                }
            }
        }
    }

    class Factory(
        private val addTransactionUseCase: AddTransactionUseCase,
        private val getSettingsUseCase: GetSettingsUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddTransactionViewModel::class.java)) {
                return AddTransactionViewModel(addTransactionUseCase, getSettingsUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
