package com.snapchef.app.core.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.snapchef.app.core.auth.AuthManager
import com.snapchef.app.features.auth.presentation.AuthNavGraph
import com.snapchef.app.features.home.presentation.MainScreen

/**
 * Top-level destination routes.
 */
object Routes {
    const val AUTH = "auth_graph"
    const val MAIN = "main_screen"
}

/**
 * Root navigation controller handling the transition between
 * unauthenticated (Auth) and authenticated (Main) flows.
 */
@Composable
fun RootNavGraph() {
    val navController = rememberNavController()
    val startDestination = if (AuthManager.isLoggedIn()) Routes.MAIN else Routes.AUTH

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(tween(400)) },
        exitTransition = { fadeOut(tween(400)) },
        popEnterTransition = { fadeIn(tween(400)) },
        popExitTransition = { fadeOut(tween(400)) }
    ) {
        composable(Routes.AUTH) {
            AuthNavGraph(
                onAuthSuccess = {
                    // Navigate to Main and remove Auth flow from backstack
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                onLogout = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }
    }
}
