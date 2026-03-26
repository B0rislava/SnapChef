package com.snapchef.app.features.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
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
import com.snapchef.app.core.theme.*

@Composable
fun SignUpScreen(
    onBack:       () -> Unit = {},
    onSignUp:     () -> Unit = {},
    onSignIn:     () -> Unit = {},
) {
    var name        by remember { mutableStateOf("") }
    var email       by remember { mutableStateOf("") }
    var password    by remember { mutableStateOf("") }
    var showPass    by remember { mutableStateOf(false) }
    var agreeTerms  by remember { mutableStateOf(false) }

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
                .offset(x = (-100).dp, y = 50.dp)
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = GreenPrimary)
                }
            }

            Spacer(Modifier.height(30.dp))

            Text(
                text       = "Join SnapChef!",
                style      = MaterialTheme.typography.headlineMedium,
                color      = GreenPrimary,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text      = "Start your journey today - Snap, cook, and enjoy.",
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
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    AuthTextField(
                        value         = name,
                        onValueChange = { name = it },
                        placeholder   = "Full Name",
                        leadingIcon   = {
                            Icon(Icons.Outlined.Person, null, tint = GreenPrimary)
                        },
                    )

                    AuthTextField(
                        value         = email,
                        onValueChange = { email = it },
                        placeholder   = "Email Address",
                        leadingIcon   = {
                            Icon(Icons.Outlined.Email, null, tint = GreenPrimary)
                        },
                        keyboardType  = KeyboardType.Email,
                    )

                    AuthTextField(
                        value         = password,
                        onValueChange = { password = it },
                        placeholder   = "Password",
                        leadingIcon   = {
                            Icon(Icons.Outlined.Lock, null, tint = GreenPrimary)
                        },
                        trailingIcon  = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(
                                    if (showPass) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    null, tint = GreenSecondary,
                                )
                            }
                        },
                        visualTransformation = if (showPass) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        keyboardType = KeyboardType.Password,
                    )

                    Row(verticalAlignment = Alignment.Top) {
                        Checkbox(
                            checked         = agreeTerms,
                            onCheckedChange = { agreeTerms = it },
                            colors          = CheckboxDefaults.colors(checkedColor = GreenPrimary),
                        )
                        Text(
                            text  = "By creating an account you agree to our Terms & Conditions and our Privacy Policy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GreenOnBackground.copy(alpha = 0.65f),
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick  = onSignUp,
                enabled  = agreeTerms,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape    = RoundedCornerShape(28.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    disabledContainerColor = GreenSecondary,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            ) {
                Text(
                    text  = "Create Account",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(24.dp))

            OrDivider()

            Spacer(Modifier.height(20.dp))

            SocialButton(label = "Sign up with Google",  emoji = "G")

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text  = "Member? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GreenOnBackground.copy(alpha = 0.6f),
                )
                TextButton(onClick = onSignIn, contentPadding = PaddingValues(0.dp)) {
                    Text(
                        text  = "Sign in",
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
private fun SignUpPreview() {
    SnapChefTheme { SignUpScreen() }
}
