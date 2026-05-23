package com.example.moneytracker.presentation.uistate

data class ProfileUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val birthday: String = "",
    val avatarUri: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
