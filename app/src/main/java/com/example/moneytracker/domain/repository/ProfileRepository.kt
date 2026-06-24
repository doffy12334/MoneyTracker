package com.example.moneytracker.domain.repository

import com.example.moneytracker.domain.model.user.ProfileUpdateResult
import com.example.moneytracker.domain.model.user.UserProfile

interface ProfileRepository {
    suspend fun getProfile(): UserProfile
    suspend fun updateProfile(profile: UserProfile): ProfileUpdateResult
}
