package com.example.data.model

data class AuthUser(
    val uid: String = "",
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false,
    val isAuthenticated: Boolean = false
) {
    val displayLabel: String
        get() = when {
            !displayName.isNullOrBlank() -> displayName
            !email.isNullOrBlank() -> email.substringBefore("@")
            isAnonymous -> "Guest Investigator"
            else -> "Unauthenticated"
        }

    val badgeTitle: String
        get() = if (isAuthenticated) "Verified Cyber Sleuth" else "Guest Explorer"
}
