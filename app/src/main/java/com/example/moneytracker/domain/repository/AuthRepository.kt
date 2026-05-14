package com.example.moneytracker.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String)
    suspend fun register(email: String, password: String)
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun verifyPasswordResetCode(code: String): String
}
