package com.snapchef.app.core.auth

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.snapchef.app.features.auth.data.remote.UserOut

object AuthManager {

    private val settings: Settings = Settings()

    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_USER_ID      = "user_id"
    private const val KEY_USER_NAME    = "user_name"
    private const val KEY_USER_EMAIL   = "user_email"

    // Token

    var accessToken: String?
        get() = settings.getStringOrNull(KEY_ACCESS_TOKEN)
        set(value) {
            if (value != null) settings[KEY_ACCESS_TOKEN] = value
            else settings.remove(KEY_ACCESS_TOKEN)
        }

    //  Current user (name + email persisted, no sensitive data)

    var currentUser: UserOut?
        get() {
            val id    = settings.getIntOrNull(KEY_USER_ID)       ?: return null
            val name  = settings.getStringOrNull(KEY_USER_NAME)  ?: return null
            val email = settings.getStringOrNull(KEY_USER_EMAIL) ?: return null
            return UserOut(id = id, name = name, email = email)
        }
        set(value) {
            if (value != null) {
                settings[KEY_USER_ID]    = value.id
                settings[KEY_USER_NAME]  = value.name
                settings[KEY_USER_EMAIL] = value.email
            } else {
                settings.remove(KEY_USER_ID)
                settings.remove(KEY_USER_NAME)
                settings.remove(KEY_USER_EMAIL)
            }
        }

    // Helpers

    fun isLoggedIn(): Boolean = accessToken != null
    
    fun logout() {
        accessToken = null
        currentUser = null
    }
}
