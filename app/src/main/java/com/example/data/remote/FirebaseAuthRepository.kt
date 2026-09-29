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

    suspend fun signIn(email: String, password: String): Result<AuthUser> = withContext(Dispatchers.IO) {
        val authInstance = auth ?: return@withContext Result.failure(Exception("Firebase Auth unavailable"))
        try {
            val authResult = authInstance.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user
            val mapped = mapToAuthUser(user)
            _currentUser.value = mapped
            Result.success(mapped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        avatarUrl: String? = null
    ): Result<AuthUser> = withContext(Dispatchers.IO) {
        val authInstance = auth ?: return@withContext Result.failure(Exception("Firebase Auth unavailable"))
        try {
            val authResult = authInstance.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user

            if (user != null && displayName.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .apply {
                        if (!avatarUrl.isNullOrBlank()) {
                            setPhotoUri(Uri.parse(avatarUrl))
                        }
                    }
                    .build()
                user.updateProfile(profileUpdates).await()
            }

            val mapped = mapToAuthUser(authInstance.currentUser)
            _currentUser.value = mapped
            Result.success(mapped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInAnonymously(): Result<AuthUser> = withContext(Dispatchers.IO) {
        val authInstance = auth ?: return@withContext Result.failure(Exception("Firebase Auth unavailable"))
        try {
            val authResult = authInstance.signInAnonymously().await()
            val mapped = mapToAuthUser(authResult.user)
            _currentUser.value = mapped
            Result.success(mapped)
        } catch (e: Exception) {
            Result.failure(e)
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
