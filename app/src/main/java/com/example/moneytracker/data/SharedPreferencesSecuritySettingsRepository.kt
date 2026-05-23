package com.example.moneytracker.data

import com.example.moneytracker.data.local.SharedPrefsManager
import com.example.moneytracker.domain.model.SecuritySettings
import com.example.moneytracker.domain.repository.SecuritySettingsRepository

class SharedPreferencesSecuritySettingsRepository(
    private val sharedPrefsManager: SharedPrefsManager
) : SecuritySettingsRepository {
    override fun getSecuritySettings(): SecuritySettings {
        return SecuritySettings(
            twoFactorEnabled = sharedPrefsManager.isTwoFactorEnabled(),
            biometricEnabled = sharedPrefsManager.isBiometricEnabled(),
            highValueProtectionEnabled = sharedPrefsManager.isHighValueProtectionEnabled()
        )
    }

    override fun setTwoFactorEnabled(enabled: Boolean) {
        sharedPrefsManager.setTwoFactorEnabled(enabled)
    }

    override fun setBiometricEnabled(enabled: Boolean) {
        sharedPrefsManager.setBiometricEnabled(enabled)
    }

    override fun setHighValueProtectionEnabled(enabled: Boolean) {
        sharedPrefsManager.setHighValueProtectionEnabled(enabled)
    }
}
