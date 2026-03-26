package com.snapchef.app.features.auth.presentation

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import com.snapchef.app.EditProfileScreen
import com.snapchef.app.ui.profile.ProfileScreen

/** Simple in-memory navigation for the auth flow. */
internal enum class AuthDestination { WELCOME, SIGN_IN, SIGN_UP, PROFILE, EDIT_PROFILE }

@Composable
fun AuthNavGraph(onAuthSuccess: () -> Unit) {
    var current by remember { mutableStateOf(AuthDestination.WELCOME) }
    var userName by remember { mutableStateOf("John Doe") }
    var userEmail by remember { mutableStateOf("john.doe@snapchef.app") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    AnimatedContent(
        targetState    = current,
        transitionSpec = {
            val enter = slideInHorizontally(
                animationSpec = tween(350),
                initialOffsetX = { if (targetState > initialState) it else -it },
            ) + fadeIn(tween(350))
            val exit = slideOutHorizontally(
                animationSpec = tween(350),
                targetOffsetX = { if (targetState > initialState) -it else it },
            ) + fadeOut(tween(350))
            enter togetherWith exit
        },
        label = "auth_nav",
    ) { destination ->
        when (destination) {
            AuthDestination.WELCOME -> WelcomeScreen(
                onGetStarted = { current = AuthDestination.SIGN_UP },
                onSignIn     = { current = AuthDestination.SIGN_IN },
            )
            AuthDestination.SIGN_IN -> SignInScreen(
                onBack   = { current = AuthDestination.WELCOME },
                onSignIn = onAuthSuccess,
                onSignUp = { current = AuthDestination.SIGN_UP },
            )
            AuthDestination.SIGN_UP -> SignUpScreen(
                onBack   = { current = AuthDestination.WELCOME },
                onSignUp = onAuthSuccess,
                onSignIn = { current = AuthDestination.SIGN_IN },
            )
            AuthDestination.PROFILE -> ProfileScreen(
                userName = userName,
                userEmail = userEmail,
                profileImageUri = profileImageUri,
                onBack = { current = AuthDestination.WELCOME },
                onLogout = { current = AuthDestination.WELCOME },
                onDeleteAccount = {
                    userName = "John Doe"
                    userEmail = "john.doe@snapchef.app"
                    profileImageUri = null
                    current = AuthDestination.WELCOME
                },
                onEditProfile = { current = AuthDestination.EDIT_PROFILE },
            )
            AuthDestination.EDIT_PROFILE -> EditProfileScreen(
                userName = userName,
                userEmail = userEmail,
                profileImageUri = profileImageUri,
                onPickImage = { profileImageUri = it },
                onSave = { newName, newEmail ->
                    userName = newName
                    userEmail = newEmail
                    current = AuthDestination.PROFILE
                },
                onCancel = { current = AuthDestination.PROFILE },
            )
        }
    }
}
