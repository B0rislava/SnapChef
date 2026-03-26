package com.snapchef.app.features.home.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.snapchef.app.core.presentation.components.MainTab
import com.snapchef.app.core.presentation.components.SnapChefBottomBar
import com.snapchef.app.core.theme.GreenPrimary
import com.snapchef.app.core.theme.GreenSecondary

@Composable
fun MainScreen() {
    var currentTab by remember { mutableStateOf(MainTab.HOME) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenSecondary)
    ) {
        // Content Area
        AnimatedContent(
            targetState = currentTab,
            label = "main_content",
        ) { tab ->
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (tab) {
                        MainTab.HOME -> "Home Screen"
                        MainTab.RECIPES -> "Recipes Screen"
                    },
                    style = MaterialTheme.typography.displayLarge,
                    color = GreenPrimary
                )
            }
        }

        SnapChefBottomBar(
            currentTab = currentTab,
            onTabSelected = { currentTab = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}
