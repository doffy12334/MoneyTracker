package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.usecase.DeleteBudgetLimitUseCase
import com.example.moneytracker.domain.usecase.DeleteSavingGoalUseCase
import com.example.moneytracker.domain.usecase.GetBudgetOverviewUseCase
import com.example.moneytracker.domain.usecase.SaveBudgetLimitUseCase
import com.example.moneytracker.domain.usecase.SaveSavingGoalUseCase
import com.example.moneytracker.presentation.uistate.BudgetUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val getBudgetOverviewUseCase: GetBudgetOverviewUseCase,
    private val saveBudgetLimitUseCase: SaveBudgetLimitUseCase,
    private val deleteBudgetLimitUseCase: DeleteBudgetLimitUseCase,
    private val saveSavingGoalUseCase: SaveSavingGoalUseCase,
    private val deleteSavingGoalUseCase: DeleteSavingGoalUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<BudgetUiState>(BudgetUiState.Loading)
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        loadBudget()
    }

    fun loadBudget() {
        viewModelScope.launch {
            _uiState.value = BudgetUiState.Loading
            _uiState.value = runCatching {
                BudgetUiState.Success(getBudgetOverviewUseCase())
            }.getOrElse { exception ->
                BudgetUiState.Error(exception.message ?: "Could not load budget")
            }
        }
    }

    fun saveBudgetLimit(category: TransactionCategory, amount: Double) {
        viewModelScope.launch {
            runCatching {
                saveBudgetLimitUseCase(category, amount)
            }.onSuccess {
                _messages.emit("Budget limit saved")
                loadBudget()
            }.onFailure { exception ->
                _messages.emit(exception.message ?: "Could not save budget limit")
            }
        }
    }

    fun deleteBudgetLimit(category: TransactionCategory) {
        viewModelScope.launch {
            deleteBudgetLimitUseCase(category)
            _messages.emit("Budget limit deleted")
            loadBudget()
        }
    }

    fun saveSavingGoal(id: String?, title: String, targetAmount: Double, currentAmount: Double) {
        viewModelScope.launch {
            runCatching {
                saveSavingGoalUseCase(id, title, targetAmount, currentAmount)
            }.onSuccess {
                _messages.emit("Saving goal saved")
                loadBudget()
            }.onFailure { exception ->
                _messages.emit(exception.message ?: "Could not save saving goal")
            }
        }
    }

    fun deleteSavingGoal(goalId: String) {
        viewModelScope.launch {
            deleteSavingGoalUseCase(goalId)
            _messages.emit("Saving goal deleted")
            loadBudget()
        }
    }

    class Factory(
        private val getBudgetOverviewUseCase: GetBudgetOverviewUseCase,
        private val saveBudgetLimitUseCase: SaveBudgetLimitUseCase,
        private val deleteBudgetLimitUseCase: DeleteBudgetLimitUseCase,
        private val saveSavingGoalUseCase: SaveSavingGoalUseCase,
        private val deleteSavingGoalUseCase: DeleteSavingGoalUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
                return BudgetViewModel(
                    getBudgetOverviewUseCase,
                    saveBudgetLimitUseCase,
                    deleteBudgetLimitUseCase,
                    saveSavingGoalUseCase,
                    deleteSavingGoalUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
