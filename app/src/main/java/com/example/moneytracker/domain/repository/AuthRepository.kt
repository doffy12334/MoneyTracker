package com.example.moneytracker.domain.repository

interface AuthRepository {
    fun isUserLoggedIn(): Boolean
    fun isCurrentUserGoogleAccount(): Boolean
    fun getCurrentUserEmail(): String?
    fun logout()
    suspend fun deleteAccount(password: String)
    suspend fun login(email: String, password: String)
    suspend fun loginWithGoogle(idToken: String)
    suspend fun register(email: String, password: String)
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun updatePassword(newPassword: String)
    suspend fun verifyPasswordResetCode(code: String): String
    suspend fun deleteCurrentUser()

    // Phone Auth
    suspend fun sendPhoneVerificationCode(phoneNumber: String, activity: Any): String
    suspend fun verifyPhoneOtp(verificationId: String, code: String)
    suspend fun linkPhoneToCurrentUser(verificationId: String, code: String)
    suspend fun resetPasswordAfterPhoneVerification(newPassword: String)
}
