package com.example.moneytracker.data

import com.example.moneytracker.data.local.SharedPrefsManager
import com.example.moneytracker.domain.model.ProfileUpdateResult
import com.example.moneytracker.domain.model.UserProfile
import com.example.moneytracker.domain.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestoreException
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
        val currentUser = firebaseAuth.currentUser ?: return localProfile
        val uid = currentUser.uid
        val authEmail = currentUser.email.orEmpty()
        val authName = currentUser.displayName.orEmpty()
        val authAvatarUri = currentUser.photoUrl?.toString().orEmpty()

        val remoteProfile = runCatching {
            val snapshot = firestore.collection("users")
                .document(uid)
                .get()
                .await()
            UserProfile(
                fullName = snapshot.getString(FIELD_FULL_NAME).orEmpty(),
                email = snapshot.getString(FIELD_EMAIL).orEmpty(),
                phone = snapshot.getString(FIELD_PHONE).orEmpty(),
                occupation = snapshot.getString(FIELD_OCCUPATION).orEmpty(),
                avatarUri = snapshot.getString(FIELD_AVATAR_URI).orEmpty()
            )
        }.getOrNull()

        val mergedProfile = localProfile.copy(
            fullName = remoteProfile?.fullName
                ?.ifBlank { localProfile.fullName }
                ?.ifBlank { authName }
                ?: localProfile.fullName.ifBlank { authName },
            email = authEmail.ifBlank {
                remoteProfile?.email?.ifBlank { localProfile.email } ?: localProfile.email
            },
            phone = remoteProfile?.phone?.ifBlank { localProfile.phone } ?: localProfile.phone,
            occupation = remoteProfile?.occupation?.ifBlank { localProfile.occupation } ?: localProfile.occupation,
            avatarUri = remoteProfile?.avatarUri
                ?.ifBlank { localProfile.avatarUri }
                ?.ifBlank { authAvatarUri }
                ?: localProfile.avatarUri.ifBlank { authAvatarUri }
        )
        saveLocalProfile(mergedProfile)
        val shouldSeedRemoteProfile =
            authEmail.isNotBlank() && !authEmail.equals(remoteProfile?.email.orEmpty(), ignoreCase = true) ||
                authName.isNotBlank() && remoteProfile?.fullName.isNullOrBlank() ||
                authAvatarUri.isNotBlank() && remoteProfile?.avatarUri.isNullOrBlank()
        if (shouldSeedRemoteProfile) {
            runCatching {
                firestore.collection("users")
                    .document(uid)
                    .set(
                        mapOf(
                            FIELD_EMAIL to authEmail,
                            FIELD_FULL_NAME to mergedProfile.fullName,
                            FIELD_AVATAR_URI to mergedProfile.avatarUri,
                            FIELD_PHONE to mergedProfile.phone,
                            FIELD_OCCUPATION to mergedProfile.occupation,
                            FIELD_PENDING_EMAIL to ""
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
        val profileToStore = profile.copy(
            email = currentAuthEmail.ifBlank { profile.email }
        )
        saveLocalProfile(profileToStore)

        firebaseAuth.currentUser?.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(profile.fullName)
                .build()
        )?.await()

        val uid = firebaseAuth.currentUser?.uid ?: return ProfileUpdateResult(
            emailVerificationSent = false
        )
        val data = mapOf(
            FIELD_FULL_NAME to profileToStore.fullName,
            FIELD_PHONE to profileToStore.phone,
            FIELD_OCCUPATION to profileToStore.occupation,
            FIELD_AVATAR_URI to profileToStore.avatarUri
        )
        try {
            firestore.collection("users")
                .document(uid)
                .set(data, SetOptions.merge())
                .await()
        } catch (exception: FirebaseFirestoreException) {
            if (exception.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                throw IllegalArgumentException(
                    "Firestore Rules chua cho phep luu ho so. Can cho phep user ghi users/$uid"
                )
            }
            throw exception
        }
        return ProfileUpdateResult(emailVerificationSent = false)
    }

    private fun getLocalProfile(): UserProfile {
        val uid = firebaseAuth.currentUser?.uid.orEmpty()
        val savedEmail = sharedPrefsManager.getProfileEmail(uid)
        val authEmail = firebaseAuth.currentUser?.email.orEmpty()
        return UserProfile(
            fullName = sharedPrefsManager.getProfileFullName(uid),
            email = savedEmail.ifBlank { authEmail },
            phone = sharedPrefsManager.getProfilePhone(uid),
            occupation = sharedPrefsManager.getProfileOccupation(uid),
            avatarUri = sharedPrefsManager.getProfileAvatarUri(uid)
        )
    }

    private fun saveLocalProfile(profile: UserProfile) {
        val uid = firebaseAuth.currentUser?.uid.orEmpty()
        sharedPrefsManager.setProfileFullName(profile.fullName, uid)
        sharedPrefsManager.setProfileEmail(profile.email, uid)
        sharedPrefsManager.setProfilePhone(profile.phone, uid)
        sharedPrefsManager.setProfileOccupation(profile.occupation, uid)
        sharedPrefsManager.setProfileAvatarUri(profile.avatarUri, uid)
    }

    private companion object {
        const val FIELD_FULL_NAME = "fullName"
        const val FIELD_EMAIL = "email"
        const val FIELD_PENDING_EMAIL = "pendingEmail"
        const val FIELD_PHONE = "phone"
        const val FIELD_OCCUPATION = "occupation"
        const val FIELD_AVATAR_URI = "avatarUri"
    }
}
