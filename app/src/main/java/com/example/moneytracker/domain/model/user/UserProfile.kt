package com.example.moneytracker.domain.model.user


data class UserProfile(
    val fullName: String,
    val email: String,
    val phone: String,
    val occupation: String,
    val avatarUri: String = ""
)
