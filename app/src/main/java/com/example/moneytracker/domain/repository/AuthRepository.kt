package com.example.moneytracker.domain.repository

interface AuthRepository {
    fun isUserLoggedIn(): Boolean
    fun logout()
    suspend fun login(email: String, password: String)
    suspend fun loginWithGoogle(idToken: String)
    suspend fun register(email: String, password: String)
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun verifyPasswordResetCode(code: String): String
}
