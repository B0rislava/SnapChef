package com.snapchef.app.core.auth

/**
 * Session persistence is iOS-only. Android keeps auth in memory for this app.
 */
actual object AuthSessionPersistence {
    actual fun save(session: PersistedSession) {}

    actual fun read(): PersistedSession? = null

    actual fun clear() {}
}

internal actual fun epochMillis(): Long = System.currentTimeMillis()
