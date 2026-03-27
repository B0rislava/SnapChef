package com.snapchef.app.core.auth

import com.snapchef.app.features.auth.data.remote.UserOut

object AuthManager {
    var accessToken: String? = null
    var currentUser: UserOut? = null

    fun isLoggedIn(): Boolean = accessToken != null
    
    fun logout() {
        accessToken = null
        currentUser = null
    }
}
