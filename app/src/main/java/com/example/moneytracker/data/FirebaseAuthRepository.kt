package com.example.moneytracker.data

import com.example.moneytracker.domain.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {
    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
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
    }

    override suspend fun register(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    override suspend fun verifyPasswordResetCode(code: String): String {
        return firebaseAuth.verifyPasswordResetCode(code).await()
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
        is FirebaseAuthInvalidUserException -> "TĂ i khoáº£n khĂ´ng tá»“n táº¡i hoáº·c Ä‘Ă£ bá»‹ vĂ´ hiá»‡u hĂ³a"
        is FirebaseAuthInvalidCredentialsException -> "Email hoáº·c máº­t kháº©u khĂ´ng Ä‘Ăºng"
        is FirebaseAuthUserCollisionException -> "Email nĂ y Ä‘Ă£ Ä‘Æ°á»£c Ä‘Äƒng kĂ½"
        is FirebaseAuthWeakPasswordException -> "Máº­t kháº©u quĂ¡ yáº¿u, vui lĂ²ng dĂ¹ng Ă­t nháº¥t 6 kĂ½ tá»±"
        is FirebaseNetworkException -> "KhĂ´ng cĂ³ káº¿t ná»‘i máº¡ng, vui lĂ²ng thá»­ láº¡i"
        is FirebaseTooManyRequestsException -> "Báº¡n thao tĂ¡c quĂ¡ nhiá»u láº§n, vui lĂ²ng thá»­ láº¡i sau"
        else -> this?.message ?: "KhĂ´ng thá»ƒ xĂ¡c thá»±c tĂ i khoáº£n"
    }
    return IllegalArgumentException(message)
}
