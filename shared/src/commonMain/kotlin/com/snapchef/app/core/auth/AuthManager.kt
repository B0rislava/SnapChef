package com.snapchef.app.core.auth

import com.snapchef.app.features.auth.data.remote.UserOut
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val userJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/** Client-side session shelf life; JWT on the server may expire separately. */
private const val SESSION_MAX_AGE_MS = 30L * 24 * 60 * 60 * 1000

object AuthManager {
    var accessToken: String? = null
    var currentUser: UserOut? = null

    fun isLoggedIn(): Boolean = accessToken != null

    /** Persists on iOS only; use from iOS after login. Android assigns [accessToken]/[currentUser] directly. */
    fun signIn(accessToken: String, user: UserOut) {
        this.accessToken = accessToken
        this.currentUser = user
        val json = userJson.encodeToString(user)
        AuthSessionPersistence.save(
            PersistedSession(accessToken, json, epochMillis())
        )
    }

    fun restoreSessionIfValid(): Boolean {
        val p = AuthSessionPersistence.read() ?: return false
        if (epochMillis() - p.savedAtEpochMs > SESSION_MAX_AGE_MS) {
            AuthSessionPersistence.clear()
            accessToken = null
            currentUser = null
            return false
        }
        accessToken = p.accessToken
        currentUser = try {
            userJson.decodeFromString<UserOut>(p.userJson)
        } catch (_: Exception) {
            null
        }
        if (currentUser == null) {
            logout()
            return false
        }
        return true
    }

    fun logout() {
        accessToken = null
        currentUser = null
        AuthSessionPersistence.clear()
    }
}
