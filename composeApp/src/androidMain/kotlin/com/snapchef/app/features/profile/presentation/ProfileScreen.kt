package com.snapchef.app.features.profile.presentation

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.snapchef.app.features.profile.presentation.components.ProfilePhoto
import com.snapchef.app.core.theme.GreenBackground
import com.snapchef.app.core.theme.GreenOnBackground
import com.snapchef.app.core.theme.GreenPrimary
import com.snapchef.app.core.theme.GreenSecondary
import com.snapchef.app.core.theme.SnapChefTheme


@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String,
    profileImageUri: Uri?,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val initials = remember(userName) { userName.toInitials() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(GreenSecondary.copy(alpha = 0.55f), GreenBackground))
            )
    ) {
        // top circle accent
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = 240.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(GreenPrimary.copy(alpha = 0.10f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Top header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Your Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = GreenPrimary,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Avatar block
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        ProfilePhoto(
                            imageUri = profileImageUri,
                            initials = initials,
                            modifier = Modifier
                                .size(128.dp)
                                .clip(CircleShape),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Fields inside a nice card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ReadOnlyProfileField(
                        label = "Name",
                        value = userName,
                    )
                    ReadOnlyProfileField(
                        label = "Email",
                        value = userEmail,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Inventory strip
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "My Inventory",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val inventory = remember {
                        listOf("Eggs", "Milk", "Cheese", "Tomatoes", "Chicken Breast", "Olive Oil", "Bread")
                    }

                    @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        inventory.forEach { item ->
                            val isPerishable = item in listOf("Eggs", "Milk", "Cheese", "Chicken Breast")
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isPerishable) GreenPrimary.copy(alpha = 0.1f) else GreenSecondary.copy(alpha = 0.3f))
                                    .border(1.dp, if (isPerishable) GreenPrimary else GreenSecondary, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isPerishable) GreenPrimary else GreenOnBackground,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bottom actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                BouncyAction(
                    modifier = Modifier.weight(1f),
                    text = "Logout",
                    container = GreenPrimary,
                    content = Color.White,
                    onClick = { showLogoutDialog = true },
                )
                BouncyAction(
                    modifier = Modifier.weight(1f),
                    text = "Delete",
                    container = GreenSecondary.copy(alpha = 0.5f),
                    content = GreenPrimary,
                    onClick = { showDeleteDialog = true },
                )
                BouncyAction(
                    modifier = Modifier.weight(1f),
                    text = "Edit",
                    container = GreenSecondary,
                    content = GreenOnBackground,
                    onClick = onEditProfile,
                )
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete account") },
            text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccount()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun ReadOnlyProfileField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            color = GreenSecondary.copy(alpha = 0.35f),
            modifier = Modifier.size(48.dp),
            contentColor = GreenPrimary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (label == "Name") Icons.Outlined.Person else Icons.Outlined.Email,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = GreenOnBackground.copy(alpha = 0.55f),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = GreenOnBackground,
            )
        }
    }
}


@Composable
private fun BouncyAction(
    text: String,
    container: Color,
    content: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(stiffness = 450f, dampingRatio = 0.65f),
        label = "actionScale",
    )

    Surface(
        modifier = Modifier
            .then(modifier)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(24.dp),
        color = container,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    onClick = onClick,
                )
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = text, color = content, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    SnapChefTheme {
        ProfileScreen(
            userName = "John Doe",
            userEmail = "john.doe@snapchef.app",
            profileImageUri = null,
            onLogout = {},
            onDeleteAccount = {},
            onEditProfile = {},
        )
    }
}

private fun String.toInitials(): String {
    val parts = trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
    if (parts.isEmpty()) return "JD"
    if (parts.size == 1) return parts.first().take(2).uppercase()
    return (parts.first().first().toString() + parts.last().first().toString()).uppercase()
}
