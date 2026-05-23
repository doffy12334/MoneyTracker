package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.UserProfile
import com.example.moneytracker.domain.model.ProfileUpdateResult
import com.example.moneytracker.domain.repository.ProfileRepository

class UpdateProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(profile: UserProfile): ProfileUpdateResult {
        val normalized = profile.copy(
            fullName = profile.fullName.trim(),
            email = profile.email.trim(),
            phone = profile.phone.trim(),
            birthday = profile.birthday.trim(),
            avatarUri = profile.avatarUri.trim()
        )

        require(normalized.fullName.isNotBlank()) { "Ho ten khong duoc de trong" }
        require(EMAIL_REGEX.matches(normalized.email)) { "Email khong hop le" }
        require(normalized.phone.isBlank() || PHONE_REGEX.matches(normalized.phone)) {
            "So dien thoai khong hop le"
        }
        require(normalized.birthday.isBlank() || BIRTHDAY_REGEX.matches(normalized.birthday)) {
            "Ngay sinh phai co dinh dang MM/DD/YYYY"
        }

        return profileRepository.updateProfile(normalized)
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        val PHONE_REGEX = Regex("^[+0-9][0-9 ]{7,18}$")
        val BIRTHDAY_REGEX = Regex("^(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])/\\d{4}$")
    }
}
