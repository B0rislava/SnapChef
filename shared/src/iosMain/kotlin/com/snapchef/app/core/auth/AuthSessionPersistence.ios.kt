package com.snapchef.app.core.auth

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSUserDefaults
import platform.posix.time

private const val K_TOKEN = "snapchef_access_token"
private const val K_USER = "snapchef_user_json"
private const val K_AT = "snapchef_saved_at_ms"

actual object AuthSessionPersistence {

    private val defaults get() = NSUserDefaults.standardUserDefaults

    actual fun save(session: PersistedSession) {
        defaults.setObject(session.accessToken, forKey = K_TOKEN)
        defaults.setObject(session.userJson, forKey = K_USER)
        defaults.setObject(session.savedAtEpochMs.toString(), forKey = K_AT)
        defaults.synchronize()
    }

    actual fun read(): PersistedSession? {
        val token = defaults.stringForKey(K_TOKEN) ?: return null
        val user = defaults.stringForKey(K_USER) ?: return null
        val atStr = defaults.stringForKey(K_AT) ?: return null
        val at = atStr.toLongOrNull() ?: return null
        if (at <= 0L) return null
        return PersistedSession(token, user, at)
    }

    actual fun clear() {
        defaults.removeObjectForKey(K_TOKEN)
        defaults.removeObjectForKey(K_USER)
        defaults.removeObjectForKey(K_AT)
        defaults.synchronize()
    }
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun epochMillis(): Long = time(null) * 1000L
