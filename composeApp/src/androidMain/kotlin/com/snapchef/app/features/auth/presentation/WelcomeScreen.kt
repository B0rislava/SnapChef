package com.snapchef.app.features.auth.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.snapchef.app.core.theme.GreenBackground
import com.snapchef.app.core.theme.GreenOnBackground
import com.snapchef.app.core.theme.GreenPrimary
import com.snapchef.app.core.theme.GreenSecondary
import com.snapchef.app.core.theme.SnapChefTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.launch

// ── Onboarding slide data ────────────────────────────────────────────────────

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val accentColor: Color,
)

private val pages = listOf(
    OnboardingPage(
        icon     = Icons.Rounded.Restaurant,
        title    = "SnapChef",
        subtitle = "Every day, tonnes of food are thrown away while thousands go hungry. It's time to change that.",
        accentColor = Color(0xFF4CAF50),
    ),
    OnboardingPage(
        icon     = Icons.Rounded.CameraAlt,
        title    = "Snap your fridge",
        subtitle = "Take a photo of whatever ingredients you have left. Our AI instantly recognises every item - no manual typing needed.",
        accentColor = Color(0xFF2E7D32),
    ),
    OnboardingPage(
        icon     = Icons.Rounded.Eco,
        title    = "Zero Left",
        subtitle = "Get personalised recipes that use exactly what you have. Less waste, more flavour, a better planet.",
        accentColor = Color(0xFF1B5E20),
    ),
)

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit = {},
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope      = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue  = 8f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "floatOffset",
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
                .size(280.dp)
                .offset(x = (-70).dp, y = (-70).dp)
                .clip(CircleShape)
                .background(GreenSecondary.copy(alpha = 0.25f))
        )
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 270.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(GreenPrimary.copy(alpha = 0.10f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // ── Pager ────────────────────────────────────────────────────
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier.weight(1f),
            ) { pageIndex ->
                val page = pages[pageIndex]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    // Floating emoji icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(140.dp)
                            .offset(y = floatOffset.dp)
                            .shadow(16.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(page.accentColor, page.accentColor.copy(alpha = 0.7f))
                                )
                            ),
                    ) {
                        Icon(
                            imageVector = page.icon,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(Modifier.height(48.dp))

                    Text(
                        text       = page.title,
                        style      = MaterialTheme.typography.displaySmall,
                        color      = GreenPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign  = TextAlign.Center,
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text      = page.subtitle,
                        style     = MaterialTheme.typography.bodyLarge,
                        color     = GreenOnBackground.copy(alpha = 0.68f),
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp,
                    )
                }
            }

            // ── Dot indicators ───────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 28.dp),
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (isSelected) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) GreenPrimary else GreenSecondary.copy(alpha = 0.45f)
                            )
                    )
                }
            }

            // ── CTA button ───────────────────────────────────────────────
            val isLastPage = pagerState.currentPage == pages.lastIndex

            Button(
                onClick = {
                    if (isLastPage) {
                        onGetStarted()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp),
                shape  = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            ) {
                Text(
                    text  = if (isLastPage) "Get Started" else "Next",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
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
