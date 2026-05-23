package com.example.moneytracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.domain.model.UserProfile
import com.example.moneytracker.domain.usecase.GetProfileUseCase
import com.example.moneytracker.domain.usecase.UpdateProfileUseCase
import com.example.moneytracker.presentation.uistate.ProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { getProfileUseCase() }
                .onSuccess { profile ->
                    _uiState.value = ProfileUiState(
                        fullName = profile.fullName,
                        email = profile.email,
                        phone = profile.phone,
                        birthday = profile.birthday,
                        avatarUri = profile.avatarUri,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Khong the tai ho so"
                        )
                    }
                }
        }
    }

    fun onProfileChanged(
        fullName: String,
        email: String,
        phone: String,
        birthday: String,
        avatarUri: String = _uiState.value.avatarUri
    ) {
        _uiState.update {
            it.copy(
                fullName = fullName,
                email = email,
                phone = phone,
                birthday = birthday,
                avatarUri = avatarUri,
                isSaved = false,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun onAvatarChanged(avatarUri: String) {
        _uiState.update {
            it.copy(
                avatarUri = avatarUri,
                isSaved = false,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun saveProfile() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, isSaved = false, errorMessage = null) }
            runCatching {
                updateProfileUseCase(
                    UserProfile(
                        fullName = state.fullName,
                        email = state.email,
                        phone = state.phone,
                        birthday = state.birthday,
                        avatarUri = state.avatarUri
                    )
                )
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        email = if (result.emailVerificationSent) state.email else it.email,
                        isSaving = false,
                        isSaved = true,
                        successMessage = if (result.emailVerificationSent) {
                            "Da gui email xac minh. Email dang nhap se doi sau khi ban bam link xac minh"
                        } else {
                            "Da luu ho so"
                        },
                        errorMessage = null
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSaved = false,
                        successMessage = null,
                        errorMessage = exception.message ?: "Khong the cap nhat ho so"
                    )
                }
            }
        }
    }

    fun consumeSaveResult() {
        _uiState.update { it.copy(isSaved = false, successMessage = null, errorMessage = null) }
    }

    class Factory(
        private val getProfileUseCase: GetProfileUseCase,
        private val updateProfileUseCase: UpdateProfileUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel(getProfileUseCase, updateProfileUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
