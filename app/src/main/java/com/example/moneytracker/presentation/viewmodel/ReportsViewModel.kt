package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.moneytracker.presentation.uistate.ReportsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReportsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()
}
