package com.example.moneytracker.domain.repository

import com.example.moneytracker.domain.model.settings.SecuritySettings

interface SecuritySettingsRepository {
    fun getSecuritySettings(): SecuritySettings
    fun setBiometricEnabled(enabled: Boolean)
    fun setHighValueProtectionEnabled(enabled: Boolean)
}
