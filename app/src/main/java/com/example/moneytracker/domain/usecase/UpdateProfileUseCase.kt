package com.example.moneytracker.domain.usecase

import com.example.moneytracker.R
import com.example.moneytracker.domain.exception.AppException
import com.example.moneytracker.domain.model.user.ProfileUpdateResult
import com.example.moneytracker.domain.model.user.UserProfile
import com.example.moneytracker.domain.repository.ProfileRepository

class UpdateProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(profile: UserProfile): ProfileUpdateResult {
        val normalized = profile.copy(
            fullName = profile.fullName.trim(),
            email = profile.email.trim(),
            phone = profile.phone.trim(),
            occupation = profile.occupation.trim(),
            avatarUri = profile.avatarUri.trim()
        )

        if (normalized.fullName.isBlank()) throw AppException(R.string.error_empty_full_name)
        if (!EMAIL_REGEX.matches(normalized.email)) throw AppException(R.string.error_invalid_email)
        if (normalized.phone.isNotBlank() && !PHONE_REGEX.matches(normalized.phone)) {
            throw AppException(R.string.error_invalid_phone)
        }

        return profileRepository.updateProfile(normalized)
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        val PHONE_REGEX = Regex("^[+0-9][0-9 ]{7,18}$")
    }
}
