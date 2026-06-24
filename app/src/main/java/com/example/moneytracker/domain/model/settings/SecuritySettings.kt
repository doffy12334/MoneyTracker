package com.example.moneytracker.domain.model.settings


data class SecuritySettings(
    val biometricEnabled: Boolean,
    val highValueProtectionEnabled: Boolean
)
