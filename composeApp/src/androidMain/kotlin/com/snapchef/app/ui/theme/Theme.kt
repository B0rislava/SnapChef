package com.snapchef.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Brand palette ───────────────────────────────────────────────────────────
val GreenPrimary    = Color(0xFF587E3D)
val GreenSecondary  = Color(0xFFCED8B7)
val GreenBackground = Color(0xFFF5FBF0)
val GreenSurface    = Color(0xFFFFFFFF)
val GreenOnPrimary  = Color(0xFFFFFFFF)
val GreenOnSecondary= Color(0xFF2D3D1A)
val GreenOnBackground = Color(0xFF1C2B10)
val GreenOnSurface  = Color(0xFF1C2B10)
val GreenError      = Color(0xFFB00020)

private val SnapChefColorScheme = lightColorScheme(
    primary          = GreenPrimary,
    onPrimary        = GreenOnPrimary,
    primaryContainer = GreenSecondary,
    onPrimaryContainer = GreenOnSecondary,
    secondary        = GreenSecondary,
    onSecondary      = GreenOnSecondary,
    background       = GreenBackground,
    onBackground     = GreenOnBackground,
    surface          = GreenSurface,
    onSurface        = GreenOnSurface,
    error            = GreenError,
)

@Composable
fun SnapChefTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SnapChefColorScheme,
        typography  = Typography,
        content     = content,
    )
}
