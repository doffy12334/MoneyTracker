package com.example.moneytracker.data

import com.example.moneytracker.domain.repository.AuthRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

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

    override fun logout() {
        firebaseAuth.signOut()
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
        val user = firebaseAuth.currentUser ?: throw IllegalArgumentException("Tài khoản chưa đăng nhập")
        user.updatePassword(newPassword).await()
    }

    override suspend fun verifyPasswordResetCode(code: String): String {
        return firebaseAuth.verifyPasswordResetCode(code).await()
    }

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

private fun Exception?.toAuthException(): IllegalArgumentException {
    val message = when (this) {
        is FirebaseAuthInvalidUserException -> "Tài khoản không tồn tại hoặc đã bị vô hiệu hóa"
        is FirebaseAuthInvalidCredentialsException -> "Email hoặc mật khẩu không đúng"
        is FirebaseAuthUserCollisionException -> "Email này đã được đăng ký"
        is FirebaseAuthWeakPasswordException -> "Mật khẩu quá yếu, vui lòng dùng ít nhất 6 ký tự"
        is FirebaseNetworkException -> "Không có kết nối mạng, vui lòng thử lại"
        is FirebaseTooManyRequestsException -> "Bạn thao tác quá nhiều lần, vui lòng thử lại sau"
        else -> this?.message ?: "Không thể xác thực tài khoản"
    }
    return IllegalArgumentException(message)
}
