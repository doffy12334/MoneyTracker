package com.example.moneytracker.data

import android.app.Activity
import com.example.moneytracker.R
import com.example.moneytracker.domain.repository.AuthRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun isCurrentUserGoogleAccount(): Boolean {
        return firebaseAuth.currentUser?.providerData
            ?.any { it.providerId == GoogleAuthProvider.PROVIDER_ID } == true
    }

    override fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override suspend fun deleteAccount(password: String) {
        val user = firebaseAuth.currentUser ?: throw IllegalArgumentException(
            "Tài khoản chưa đăng nhập")
        val email = user.email.orEmpty()
        val hasPasswordProvider = user.providerData.any {
            it.providerId == EmailAuthProvider.PROVIDER_ID
        }
        if (email.isBlank() || !hasPasswordProvider) {
            throw IllegalArgumentException(
                "Tài khoản này không dùng mật khẩu. Vui lòng đăng nhập" +
                        " lại bằng Google để xóa tài khoản")
        }
        val uid = user.uid
        val credential = EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(credential).await()
        user.delete().await()
        runCatching {
            firestore.collection("users").document(uid).delete().await()
        }
    }

    override suspend fun login(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun loginWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).await()
        seedCurrentUserProfile()
    }

    override suspend fun register(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        firebaseAuth.currentUser?.sendEmailVerification()?.await()
        seedCurrentUserProfile()
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    override suspend fun updatePassword(newPassword: String) {
        val user = firebaseAuth.currentUser ?: throw AuthException(R.string.error_unknown)
        user.updatePassword(newPassword).await()
    }

    override suspend fun verifyPasswordResetCode(code: String): String {
        return firebaseAuth.verifyPasswordResetCode(code).await()
    }

    override suspend fun deleteCurrentUser() {
        firebaseAuth.currentUser?.delete()?.await()
    }

    // ── Phone Auth ───────────────────────────────────────────────────────

    private var autoVerifiedCredential: PhoneAuthCredential? = null

    override suspend fun sendPhoneVerificationCode(
        phoneNumber: String,
        activity: Any
    ): String = suspendCancellableCoroutine { continuation ->
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                autoVerifiedCredential = credential
                if (continuation.isActive) {
                    continuation.resume("AUTO_VERIFIED")
                }
            }

            override fun onVerificationFailed(exception: com.google.firebase.FirebaseException) {
                if (continuation.isActive) {
                    continuation.resumeWithException(exception.toAuthException())
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                if (continuation.isActive) {
                    continuation.resume(verificationId)
                }
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(OTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .setActivity(activity as Activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override suspend fun verifyPhoneOtp(verificationId: String, code: String) {
        val credential = if (verificationId == "AUTO_VERIFIED" && autoVerifiedCredential != null) {
            autoVerifiedCredential!!
        } else {
            PhoneAuthProvider.getCredential(verificationId, code)
        }
        firebaseAuth.signInWithCredential(credential).await()
        autoVerifiedCredential = null
    }

    override suspend fun linkPhoneToCurrentUser(verificationId: String, code: String) {
        val credential = if (verificationId == "AUTO_VERIFIED" && autoVerifiedCredential != null) {
            autoVerifiedCredential!!
        } else {
            PhoneAuthProvider.getCredential(verificationId, code)
        }
        val user = firebaseAuth.currentUser ?: throw AuthException(R.string.error_unknown)
        user.linkWithCredential(credential).await()
        autoVerifiedCredential = null
    }

    override suspend fun resetPasswordAfterPhoneVerification(newPassword: String) {
        val user = firebaseAuth.currentUser ?: throw AuthException(R.string.error_unknown)
        user.updatePassword(newPassword).await()
        // Sign out so the user must log back in with the new password
        firebaseAuth.signOut()
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private suspend fun seedCurrentUserProfile() {
        val user = firebaseAuth.currentUser ?: return
        firestore.collection("users")
            .document(user.uid)
            .set(
                mapOf(
                    "email" to user.email.orEmpty(),
                    "fullName" to user.displayName.orEmpty(),
                    "avatarUri" to user.photoUrl?.toString().orEmpty(),
                    "phone" to user.phoneNumber.orEmpty(),
                    "occupation" to "",
                    "pendingEmail" to ""
                ),
                SetOptions.merge()
            )
            .await()
    }

    private companion object {
        const val OTP_TIMEOUT_SECONDS = 60L
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception.toAuthException())
        }
    }
}

private fun Exception?.toAuthException(): AuthException {
    val stringRes = when (this) {
        is FirebaseAuthInvalidUserException -> R.string.error_invalid_user
        is FirebaseAuthInvalidCredentialsException -> R.string.error_invalid_credentials
        is FirebaseAuthUserCollisionException -> R.string.error_user_collision
        is FirebaseAuthWeakPasswordException -> R.string.error_weak_password
        is FirebaseAuthRecentLoginRequiredException -> R.string.error_recent_login_required
        is FirebaseNetworkException -> R.string.error_network
        is FirebaseTooManyRequestsException -> R.string.error_too_many_requests
        else -> R.string.error_unknown
    }
    return AuthException(stringRes, this?.message)
}

class AuthException(val messageResId: Int, override val message: String? = null) : Exception()
