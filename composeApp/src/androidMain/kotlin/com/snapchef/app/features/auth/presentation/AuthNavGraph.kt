package com.snapchef.app.features.auth.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/** In-memory auth navigation. Sign-up is iOS-only; Android uses welcome → sign-in → verify (if needed). */
enum class AuthDestination { WELCOME, SIGN_IN, VERIFY }

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
                onGetStarted = { viewModel.goTo(AuthDestination.SIGN_IN) },
            )
            AuthDestination.SIGN_IN -> SignInScreen(
                onBack = { viewModel.goTo(AuthDestination.WELCOME) },
                onSignIn = onAuthSuccess,
                onVerifyRequired = { email -> viewModel.goToVerify(email) }
            )
            AuthDestination.VERIFY -> VerificationScreen(
                email = viewModel.emailToVerify ?: "",
                onBack = { viewModel.goTo(AuthDestination.SIGN_IN) },
                onSuccess = onAuthSuccess
            )
        }
    }
}
