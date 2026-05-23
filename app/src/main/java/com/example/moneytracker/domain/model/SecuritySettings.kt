package com.example.moneytracker.domain.model

data class SecuritySettings(
    val twoFactorEnabled: Boolean,
    val biometricEnabled: Boolean,
    val highValueProtectionEnabled: Boolean
)
