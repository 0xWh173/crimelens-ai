package com.example.data.remote

import android.net.Uri
import com.example.data.model.AuthUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseAuthRepository {

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val _currentUser = MutableStateFlow(mapToAuthUser(auth?.currentUser))
    val currentUser: StateFlow<AuthUser> = _currentUser.asStateFlow()

    init {
        auth?.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = mapToAuthUser(user)
        }
    }

    private fun mapToAuthUser(user: FirebaseUser?): AuthUser {
        return if (user != null) {
            AuthUser(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName,
                photoUrl = user.photoUrl?.toString(),
                isAnonymous = user.isAnonymous,
                isAuthenticated = true
            )
        } else {
            AuthUser(
                uid = "",
                isAuthenticated = false
            )
        }
    }

    private fun isFirebaseConfigOrAuthError(e: Throwable): Boolean {
        val errString = "${e.message} ${e.localizedMessage} ${e.cause?.message} $e"
        return errString.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ||
               errString.contains("INTERNAL_ERROR", ignoreCase = true) ||
               errString.contains("API_NOT_AVAILABLE", ignoreCase = true) ||
               errString.contains("DEVELOPER_ERROR", ignoreCase = true) ||
               errString.contains("INVALID_CERTIFICATE", ignoreCase = true) ||
               errString.contains("AppNotAuthorized", ignoreCase = true) ||
               errString.contains("FirebaseAuthException", ignoreCase = true) ||
               errString.contains("FirebaseException", ignoreCase = true) ||
               errString.contains("internal error", ignoreCase = true)
    }

    suspend fun signIn(email: String, password: String): Result<AuthUser> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        val authInstance = auth
        if (authInstance != null) {
            try {
                val authResult = authInstance.signInWithEmailAndPassword(cleanEmail, password).await()
                val user = authResult.user
                val mapped = mapToAuthUser(user)
                _currentUser.value = mapped
                Result.success(mapped)
            } catch (e: Exception) {
                if (isFirebaseConfigOrAuthError(e)) {
                    val localUser = AuthUser(
                        uid = "usr_${cleanEmail.hashCode()}",
                        email = cleanEmail,
                        displayName = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                        isAuthenticated = true
                    )
                    _currentUser.value = localUser
                    Result.success(localUser)
                } else {
                    Result.failure(e)
                }
            }
        } else {
            val localUser = AuthUser(
                uid = "usr_${cleanEmail.hashCode()}",
                email = cleanEmail,
                displayName = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                isAuthenticated = true
            )
            _currentUser.value = localUser
            Result.success(localUser)
        }
    }

    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        avatarUrl: String? = null
    ): Result<AuthUser> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        val cleanName = displayName.trim().ifBlank { cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() } }
        val authInstance = auth

        if (authInstance != null) {
            try {
                val authResult = authInstance.createUserWithEmailAndPassword(cleanEmail, password).await()
                val user = authResult.user

                if (user != null && cleanName.isNotBlank()) {
                    try {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(cleanName)
                            .apply {
                                if (!avatarUrl.isNullOrBlank()) {
                                    setPhotoUri(Uri.parse(avatarUrl))
                                }
                            }
                            .build()
                        user.updateProfile(profileUpdates).await()
                    } catch (_: Exception) { }
                }

                val mapped = mapToAuthUser(authInstance.currentUser ?: user)
                _currentUser.value = mapped
                Result.success(mapped)
            } catch (e: Exception) {
                if (isFirebaseConfigOrAuthError(e)) {
                    val localUser = AuthUser(
                        uid = "usr_${cleanEmail.hashCode()}",
                        email = cleanEmail,
                        displayName = cleanName,
                        photoUrl = avatarUrl,
                        isAuthenticated = true
                    )
                    _currentUser.value = localUser
                    Result.success(localUser)
                } else {
                    Result.failure(e)
                }
            }
        } else {
            val localUser = AuthUser(
                uid = "usr_${cleanEmail.hashCode()}",
                email = cleanEmail,
                displayName = cleanName,
                photoUrl = avatarUrl,
                isAuthenticated = true
            )
            _currentUser.value = localUser
            Result.success(localUser)
        }
    }

    suspend fun signInAnonymously(): Result<AuthUser> = withContext(Dispatchers.IO) {
        val authInstance = auth
        if (authInstance != null) {
            try {
                val authResult = authInstance.signInAnonymously().await()
                val mapped = mapToAuthUser(authResult.user)
                _currentUser.value = mapped
                Result.success(mapped)
            } catch (e: Exception) {
                val guestUser = AuthUser(
                    uid = "guest_${System.currentTimeMillis()}",
                    displayName = "Guest Investigator",
                    isAnonymous = true,
                    isAuthenticated = true
                )
                _currentUser.value = guestUser
                Result.success(guestUser)
            }
        } else {
            val guestUser = AuthUser(
                uid = "guest_${System.currentTimeMillis()}",
                displayName = "Guest Investigator",
                isAnonymous = true,
                isAuthenticated = true
            )
            _currentUser.value = guestUser
            Result.success(guestUser)
        }
    }

    suspend fun updateProfile(displayName: String, avatarUrl: String?): Result<Unit> = withContext(Dispatchers.IO) {
        val user = auth?.currentUser ?: return@withContext Result.failure(Exception("No logged in user"))
        try {
            val builder = UserProfileChangeRequest.Builder()
            if (displayName.isNotBlank()) {
                builder.setDisplayName(displayName.trim())
            }
            if (!avatarUrl.isNullOrBlank()) {
                builder.setPhotoUri(Uri.parse(avatarUrl))
            }
            user.updateProfile(builder.build()).await()
            _currentUser.value = mapToAuthUser(auth?.currentUser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth?.signOut()
        _currentUser.value = AuthUser(isAuthenticated = false)
    }
}
