package com.example.moneytracker.domain.repository

import com.example.moneytracker.domain.model.SecuritySettings

interface SecuritySettingsRepository {
    fun getSecuritySettings(): SecuritySettings
    fun setTwoFactorEnabled(enabled: Boolean)
    fun setBiometricEnabled(enabled: Boolean)
    fun setHighValueProtectionEnabled(enabled: Boolean)
}
