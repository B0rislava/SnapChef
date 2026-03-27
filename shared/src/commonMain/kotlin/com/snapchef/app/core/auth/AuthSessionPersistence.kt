package com.snapchef.app.core.auth

data class PersistedSession(
    val accessToken: String,
    val userJson: String,
    val savedAtEpochMs: Long,
)

/** iOS: UserDefaults. Android: no-op (in-memory auth only). */
expect object AuthSessionPersistence {
    fun save(session: PersistedSession)
    fun read(): PersistedSession?
    fun clear()
}

internal expect fun epochMillis(): Long
