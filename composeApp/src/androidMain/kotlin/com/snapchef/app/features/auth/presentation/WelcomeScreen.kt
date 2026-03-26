package com.snapchef.app.features.auth.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapchef.app.core.theme.*

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit = {},
    onSignIn:     () -> Unit = {},
) {
    val infiniteTransition = rememberInfiniteTransition(label = "leaf_anim")
    val leafOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue  = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "leafOffset",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(GreenBackground, GreenSecondary.copy(alpha = 0.45f))
                )
            ),
    ) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-60).dp, y = (-60).dp)
                .clip(CircleShape)
                .background(GreenSecondary.copy(alpha = 0.35f))
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = 260.dp, y = (-30).dp)
                .clip(CircleShape)
                .background(GreenPrimary.copy(alpha = 0.12f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .offset(y = leafOffset.dp)
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .background(GreenPrimary),
            ) {
                Text(
                    text     = "🍽️",
                    fontSize = 64.sp,
                )
            }

            Spacer(Modifier.height(40.dp))

            Text(
                text       = "SnapChef",
                style      = MaterialTheme.typography.displayLarge,
                color      = GreenPrimary,
                fontWeight = FontWeight.ExtraBold,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text      = "Snap a photo, discover a recipe.\nCook smarter every day.",
                style     = MaterialTheme.typography.bodyLarge,
                color     = GreenOnBackground.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(GreenPrimary))
                Box(Modifier.size(7.dp).clip(CircleShape).background(GreenSecondary))
                Box(Modifier.size(7.dp).clip(CircleShape).background(GreenSecondary))
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick  = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape    = RoundedCornerShape(28.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            ) {
                Text(
                    text  = "Get Started",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text  = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GreenOnBackground.copy(alpha = 0.6f),
                )
                TextButton(onClick = onSignIn, contentPadding = PaddingValues(0.dp)) {
                    Text(
                        text  = "Sign In",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GreenPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomePreview() {
    SnapChefTheme { WelcomeScreen() }
}
