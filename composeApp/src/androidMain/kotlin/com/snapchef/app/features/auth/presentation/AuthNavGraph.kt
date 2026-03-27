package com.snapchef.app.features.auth.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/** Simple in-memory navigation for the auth flow. */
enum class AuthDestination { WELCOME, SIGN_IN, SIGN_UP, VERIFY }

@Composable
fun AuthNavGraph(
    onAuthSuccess: () -> Unit,
    viewModel: AuthNavViewModel = viewModel(),
) {
    val current by viewModel.current.collectAsStateWithLifecycle()

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
                onGetStarted = { viewModel.goTo(AuthDestination.SIGN_UP) },
            )
            AuthDestination.SIGN_IN -> SignInScreen(
                onBack = { viewModel.goTo(AuthDestination.WELCOME) },
                onSignIn = onAuthSuccess,
                onSignUp = { viewModel.goTo(AuthDestination.SIGN_UP) },
                onVerifyRequired = { email -> viewModel.goToVerify(email) }
            )
            AuthDestination.SIGN_UP -> SignUpScreen(
                onBack = { viewModel.goTo(AuthDestination.WELCOME) },
                onSuccess = onAuthSuccess,
                onVerifyRequired = { email -> viewModel.goToVerify(email) },
                onSignIn = { viewModel.goTo(AuthDestination.SIGN_IN) },
            )
            AuthDestination.VERIFY -> VerificationScreen(
                email = viewModel.emailToVerify ?: "",
                onBack = { viewModel.goTo(AuthDestination.SIGN_UP) },
                onSuccess = onAuthSuccess
            )
        }
    }
}
