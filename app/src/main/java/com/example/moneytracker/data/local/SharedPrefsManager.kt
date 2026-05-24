package com.example.moneytracker.data.local

import android.content.Context
import androidx.core.content.edit

class SharedPrefsManager(context: Context) {
    private val sharedPref = context.getSharedPreferences("money_tracker_prefs", Context.MODE_PRIVATE)

    fun setNotificationEnabled(enabled: Boolean) {
        sharedPref.edit { putBoolean(KEY_NOTIFICATION_ENABLED, enabled) }
    }

    fun isNotificationEnabled(): Boolean = sharedPref.getBoolean(KEY_NOTIFICATION_ENABLED, true)

    fun setLanguageCode(languageCode: String) {
        sharedPref.edit { putString(KEY_LANGUAGE_CODE, languageCode) }
    }

    fun getLanguageCode(): String = sharedPref.getString(KEY_LANGUAGE_CODE, "vi") ?: "vi"

    fun setThemeValue(themeValue: String) {
        sharedPref.edit { putString(KEY_THEME_VALUE, themeValue) }
    }

    fun getThemeValue(): String = sharedPref.getString(KEY_THEME_VALUE, "light") ?: "light"

    fun setCurrencyCode(currencyCode: String) {
        sharedPref.edit { putString(KEY_CURRENCY_CODE, currencyCode) }
    }

    fun getCurrencyCode(): String = sharedPref.getString(KEY_CURRENCY_CODE, "VND") ?: "VND"

    fun setProfileFullName(fullName: String, userId: String = "") {
        sharedPref.edit { putString(profileKey(KEY_PROFILE_FULL_NAME, userId), fullName) }
    }

    fun getProfileFullName(userId: String = ""): String =
        sharedPref.getString(profileKey(KEY_PROFILE_FULL_NAME, userId), "") ?: ""

    fun setProfileEmail(email: String, userId: String = "") {
        sharedPref.edit { putString(profileKey(KEY_PROFILE_EMAIL, userId), email) }
    }

    fun getProfileEmail(userId: String = ""): String =
        sharedPref.getString(profileKey(KEY_PROFILE_EMAIL, userId), "") ?: ""

    fun setProfilePhone(phone: String, userId: String = "") {
        sharedPref.edit { putString(profileKey(KEY_PROFILE_PHONE, userId), phone) }
    }

    fun getProfilePhone(userId: String = ""): String =
        sharedPref.getString(profileKey(KEY_PROFILE_PHONE, userId), "") ?: ""

    fun setProfileOccupation(occupation: String, userId: String = "") {
        sharedPref.edit { putString(profileKey(KEY_PROFILE_OCCUPATION, userId), occupation) }
    }

    fun getProfileOccupation(userId: String = ""): String =
        sharedPref.getString(profileKey(KEY_PROFILE_OCCUPATION, userId), "") ?: ""

    fun setProfileAvatarUri(avatarUri: String, userId: String = "") {
        sharedPref.edit { putString(profileKey(KEY_PROFILE_AVATAR_URI, userId), avatarUri) }
    }

    fun getProfileAvatarUri(userId: String = ""): String =
        sharedPref.getString(profileKey(KEY_PROFILE_AVATAR_URI, userId), "") ?: ""

    private fun profileKey(baseKey: String, userId: String): String {
        return if (userId.isBlank()) baseKey else "${baseKey}_$userId"
    }

    fun setTwoFactorEnabled(enabled: Boolean) {
        sharedPref.edit { putBoolean(KEY_TWO_FACTOR_ENABLED, enabled) }
    }

    fun isTwoFactorEnabled(): Boolean = sharedPref.getBoolean(KEY_TWO_FACTOR_ENABLED, false)

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPref.edit { putBoolean(KEY_BIOMETRIC_ENABLED, enabled) }
    }

    fun isBiometricEnabled(): Boolean = sharedPref.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun setHighValueProtectionEnabled(enabled: Boolean) {
        sharedPref.edit { putBoolean(KEY_HIGH_VALUE_PROTECTION_ENABLED, enabled) }
    }

    fun isHighValueProtectionEnabled(): Boolean =
        sharedPref.getBoolean(KEY_HIGH_VALUE_PROTECTION_ENABLED, false)


    private companion object {
        const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        const val KEY_LANGUAGE_CODE = "language_code"
        const val KEY_THEME_VALUE = "theme_value"
        const val KEY_CURRENCY_CODE = "currency_code"
        const val KEY_PROFILE_FULL_NAME = "profile_full_name"
        const val KEY_PROFILE_EMAIL = "profile_email"
        const val KEY_PROFILE_PHONE = "profile_phone"
        const val KEY_PROFILE_OCCUPATION = "profile_occupation"
        const val KEY_PROFILE_AVATAR_URI = "profile_avatar_uri"
        const val KEY_TWO_FACTOR_ENABLED = "two_factor_enabled"
        const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        const val KEY_HIGH_VALUE_PROTECTION_ENABLED = "high_value_protection_enabled"
    }
}
