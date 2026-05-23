package com.example.moneytracker.domain.repository

import com.example.moneytracker.domain.model.UserProfile
import com.example.moneytracker.domain.model.ProfileUpdateResult

interface ProfileRepository {
    suspend fun getProfile(): UserProfile
    suspend fun updateProfile(profile: UserProfile): ProfileUpdateResult
}
