package com.snapchef.app.features.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.snapchef.app.core.presentation.components.AuthTextField
import com.snapchef.app.core.presentation.components.OrDivider
import com.snapchef.app.core.presentation.components.SocialButton
import com.snapchef.app.core.theme.GreenBackground
import com.snapchef.app.core.theme.GreenOnBackground
import com.snapchef.app.core.theme.GreenPrimary
import com.snapchef.app.core.theme.GreenSecondary
import com.snapchef.app.core.theme.SnapChefTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SignInScreen(
    onBack:       () -> Unit = {},
    onSignIn:     () -> Unit = {},
    onSignUp:     () -> Unit = {},
    onVerifyRequired: (String) -> Unit = {},
    viewModel: SignInViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(GreenSecondary.copy(alpha = 0.55f), GreenBackground))
            )
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = 220.dp, y = (-50).dp)
                .clip(CircleShape)
                .background(GreenPrimary.copy(alpha = 0.10f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth()) {
                IconButton(
                    onClick  = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary.copy(alpha = 0.10f))
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GreenPrimary,
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            Text(
                text       = "Welcome back!",
                style      = MaterialTheme.typography.headlineMedium,
                color      = GreenPrimary,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text      = "Sign in to continue your culinary journey.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = GreenOnBackground.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(24.dp),
                colors   = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp),
                ) {
                    AuthTextField(
                        value         = uiState.email,
                        onValueChange = viewModel::updateEmail,
                        placeholder   = "Enter your email",
                        leadingIcon   = {
                            Icon(Icons.Outlined.Email, null, tint = GreenPrimary)
                        },
                        keyboardType  = KeyboardType.Email,
                    )

                    Spacer(Modifier.height(16.dp))

                    AuthTextField(
                        value         = uiState.password,
                        onValueChange = viewModel::updatePassword,
                        placeholder   = "Enter your password",
                        leadingIcon   = {
                            Icon(Icons.Outlined.Lock, null, tint = GreenPrimary)
                        },
                        trailingIcon  = {
                            IconButton(onClick = viewModel::toggleShowPassword) {
                                Icon(
                                    if (uiState.showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = "Toggle password",
                                    tint = GreenSecondary,
                                )
                            }
                        },
                        visualTransformation = if (uiState.showPassword) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        keyboardType = KeyboardType.Password,
                    )

                    Spacer(Modifier.height(8.dp))

                    TextButton(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text  = "Forgot Password?",
                            color = GreenPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            uiState.errorMessage?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick  = { viewModel.signIn(onSuccess = onSignIn, onVerifyRequired = onVerifyRequired) },
                enabled  = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape    = RoundedCornerShape(28.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            ) {
                if (uiState.isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Login", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }

            Spacer(Modifier.height(24.dp))

            OrDivider()

            Spacer(Modifier.height(20.dp))

            SocialButton(label = "Continue with Google",  emoji = "G")
            Spacer(Modifier.height(12.dp))
            SocialButton(label = "Continue with Facebook", emoji = "f")
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignInPreview() {
    SnapChefTheme { SignInScreen() }
}
