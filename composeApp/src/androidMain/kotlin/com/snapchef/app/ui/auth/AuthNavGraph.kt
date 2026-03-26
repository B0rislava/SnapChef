package com.snapchef.app.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*

/** Simple in-memory navigation for the auth flow. */
enum class AuthDestination { WELCOME, SIGN_IN, SIGN_UP }

@Composable
fun AuthNavGraph() {
    var current by remember { mutableStateOf(AuthDestination.WELCOME) }

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
                onSignIn = { /* TODO: navigate to main app */ },
                onSignUp = { current = AuthDestination.SIGN_UP },
            )
            AuthDestination.SIGN_UP -> SignUpScreen(
                onBack   = { current = AuthDestination.WELCOME },
                onSignUp = { /* TODO: navigate to main app */ },
                onSignIn = { current = AuthDestination.SIGN_IN },
            )
        }
    }
}
