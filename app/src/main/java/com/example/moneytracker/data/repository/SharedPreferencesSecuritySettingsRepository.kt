package com.example.moneytracker.data.repository

import  com.example.moneytracker.data.local.SharedPrefsManager
import com.example.moneytracker.domain.model.settings.SecuritySettings
import com.example.moneytracker.domain.repository.SecuritySettingsRepository

class SharedPreferencesSecuritySettingsRepository(
    private val sharedPrefsManager: SharedPrefsManager
) : SecuritySettingsRepository {
    override fun getSecuritySettings(): SecuritySettings {
        return SecuritySettings(
            highValueProtectionEnabled = sharedPrefsManager.isHighValueProtectionEnabled())
    }

    override fun setHighValueProtectionEnabled(enabled: Boolean) {
        sharedPrefsManager.setHighValueProtectionEnabled(enabled)
    }
}
