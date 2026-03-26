package com.snapchef.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.snapchef.app.core.navigation.RootNavGraph
import com.snapchef.app.core.theme.SnapChefTheme

@Composable
@Preview
fun App() {
    SnapChefTheme {
        RootNavGraph()
    }
}