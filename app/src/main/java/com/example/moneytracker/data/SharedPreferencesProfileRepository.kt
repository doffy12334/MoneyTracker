package com.example.moneytracker.data

import com.example.moneytracker.data.local.SharedPrefsManager
import com.example.moneytracker.domain.model.ProfileUpdateResult
import com.example.moneytracker.domain.model.UserProfile
import com.example.moneytracker.domain.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class SharedPreferencesProfileRepository(
    private val sharedPrefsManager: SharedPrefsManager,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProfileRepository {
    override suspend fun getProfile(): UserProfile {
        val localProfile = getLocalProfile()
        val uid = firebaseAuth.currentUser?.uid ?: return localProfile
        val authEmail = firebaseAuth.currentUser?.email.orEmpty()

        val remoteProfile = runCatching {
            val snapshot = firestore.collection("users")
                .document(uid)
                .get()
                .await()
            UserProfile(
                fullName = snapshot.getString(FIELD_FULL_NAME).orEmpty(),
                email = snapshot.getString(FIELD_EMAIL).orEmpty(),
                phone = snapshot.getString(FIELD_PHONE).orEmpty(),
                birthday = snapshot.getString(FIELD_BIRTHDAY).orEmpty(),
                avatarUri = snapshot.getString(FIELD_AVATAR_URI).orEmpty()
            )
        }.getOrNull()

        val mergedProfile = localProfile.copy(
            fullName = remoteProfile?.fullName?.ifBlank { localProfile.fullName } ?: localProfile.fullName,
            email = authEmail.ifBlank {
                remoteProfile?.email?.ifBlank { localProfile.email } ?: localProfile.email
            },
            phone = remoteProfile?.phone?.ifBlank { localProfile.phone } ?: localProfile.phone,
            birthday = remoteProfile?.birthday?.ifBlank { localProfile.birthday } ?: localProfile.birthday,
            avatarUri = remoteProfile?.avatarUri?.ifBlank { localProfile.avatarUri } ?: localProfile.avatarUri
        )
        saveLocalProfile(mergedProfile)
        if (authEmail.isNotBlank() && !authEmail.equals(remoteProfile?.email.orEmpty(), ignoreCase = true)) {
            runCatching {
                firestore.collection("users")
                    .document(uid)
                    .set(
                        mapOf(
                            FIELD_EMAIL to authEmail,
                            FIELD_PENDING_EMAIL to "",
                            FIELD_UPDATED_AT to FieldValue.serverTimestamp()
                        ),
                        SetOptions.merge()
                    )
                    .await()
            }
        }
        return mergedProfile
    }

    override suspend fun updateProfile(profile: UserProfile): ProfileUpdateResult {
        val currentUser = firebaseAuth.currentUser
        val currentAuthEmail = currentUser?.email.orEmpty()
        val isEmailChanged = currentAuthEmail.isNotBlank() &&
            !profile.email.equals(currentAuthEmail, ignoreCase = true)

        if (isEmailChanged) {
            try {
                currentUser?.verifyBeforeUpdateEmail(profile.email)?.await()
            } catch (exception: FirebaseAuthRecentLoginRequiredException) {
                throw IllegalArgumentException("Can dang nhap lai gan day de doi email dang nhap")
            }
        }

        val profileToStore = if (isEmailChanged) {
            profile.copy(email = currentAuthEmail)
        } else {
            profile
        }
        saveLocalProfile(profileToStore)

        firebaseAuth.currentUser?.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(profile.fullName)
                .build()
        )?.await()

        val uid = firebaseAuth.currentUser?.uid ?: return ProfileUpdateResult(
            emailVerificationSent = isEmailChanged
        )
        val data = mapOf(
            FIELD_FULL_NAME to profileToStore.fullName,
            FIELD_EMAIL to profileToStore.email,
            FIELD_PENDING_EMAIL to if (isEmailChanged) profile.email else "",
            FIELD_PHONE to profileToStore.phone,
            FIELD_BIRTHDAY to profileToStore.birthday,
            FIELD_AVATAR_URI to profileToStore.avatarUri,
            FIELD_UPDATED_AT to FieldValue.serverTimestamp()
        )
        firestore.collection("users")
            .document(uid)
            .set(data, SetOptions.merge())
            .await()
        return ProfileUpdateResult(emailVerificationSent = isEmailChanged)
    }

    private fun getLocalProfile(): UserProfile {
        val savedEmail = sharedPrefsManager.getProfileEmail()
        val authEmail = firebaseAuth.currentUser?.email.orEmpty()
        return UserProfile(
            fullName = sharedPrefsManager.getProfileFullName(),
            email = savedEmail.ifBlank { authEmail },
            phone = sharedPrefsManager.getProfilePhone(),
            birthday = sharedPrefsManager.getProfileBirthday(),
            avatarUri = sharedPrefsManager.getProfileAvatarUri()
        )
    }

    private fun saveLocalProfile(profile: UserProfile) {
        sharedPrefsManager.setProfileFullName(profile.fullName)
        sharedPrefsManager.setProfileEmail(profile.email)
        sharedPrefsManager.setProfilePhone(profile.phone)
        sharedPrefsManager.setProfileBirthday(profile.birthday)
        sharedPrefsManager.setProfileAvatarUri(profile.avatarUri)
    }

    private companion object {
        const val FIELD_FULL_NAME = "fullName"
        const val FIELD_EMAIL = "email"
        const val FIELD_PENDING_EMAIL = "pendingEmail"
        const val FIELD_PHONE = "phone"
        const val FIELD_BIRTHDAY = "birthday"
        const val FIELD_AVATAR_URI = "avatarUri"
        const val FIELD_UPDATED_AT = "updatedAt"
    }
}
