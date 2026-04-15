package com.snapchef.app.features.groups.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snapchef.app.core.auth.AuthManager
import com.snapchef.app.core.theme.GreenBackground
import com.snapchef.app.core.theme.GreenOnBackground
import com.snapchef.app.core.theme.GreenPrimary
import com.snapchef.app.core.theme.GreenSecondary
import com.snapchef.app.core.theme.SnapChefTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    modifier: Modifier = Modifier,
    viewModel: GroupsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val visibleGroups = uiState.groups.filterNot { it.isPersonal }
    val selectedGroup = visibleGroups.find { it.id == uiState.selectedGroupId } ?: visibleGroups.firstOrNull()
    val isAdmin = selectedGroup?.ownerUsername.equals("You", ignoreCase = true) || 
                  (selectedGroup != null && AuthManager.currentUser?.id != null &&
                   selectedGroup.id.toIntOrNull() == AuthManager.currentUser?.id) // Fallback for ID match
    val isCodeLoading = selectedGroup != null && !selectedGroup.isPersonal && isAdmin && selectedGroup.code == null
    var groupNameInput by remember(selectedGroup?.id) { mutableStateOf(selectedGroup?.name.orEmpty()) }
    var isEditingGroupName by remember(selectedGroup?.id) { mutableStateOf(false) }
    var showLeaveConfirmation by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var memberToKick by remember { mutableStateOf<GroupMember?>(null) }

    LaunchedEffect(visibleGroups, uiState.selectedGroupId) {
        if (visibleGroups.isNotEmpty() && uiState.selectedGroupId !in visibleGroups.map { it.id }) {
            viewModel.selectGroup(visibleGroups.first().id)
        }
    }
    LaunchedEffect(uiState.infoMessage) {
        if (uiState.infoMessage != null) {
            delay(2500)
            viewModel.clearInfoMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(GreenSecondary.copy(alpha = 0.55f), GreenBackground)
                )
            ),
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = 240.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(GreenPrimary.copy(alpha = 0.10f))
        )

        AnimatedVisibility(
            visible = uiState.infoMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
                .zIndex(100f)
        ) {
            uiState.infoMessage?.let { msg ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isError) Color(0xFFC62828) else GreenPrimary
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isError) Icons.Filled.Warning else Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = msg,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Groups",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GreenPrimary,
                    fontWeight = FontWeight.ExtraBold,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GreenPrimary.copy(alpha = 0.10f)),
                        onClick = { viewModel.refreshGroups() },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh groups",
                            tint = GreenPrimary,
                        )
                    }

                    IconButton(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GreenPrimary.copy(alpha = 0.10f)),
                        onClick = { viewModel.openDialog(GroupDialogMode.CHOICE) },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Join or create group",
                            tint = GreenPrimary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Your groups",
                        style = MaterialTheme.typography.titleLarge,
                        color = GreenPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        visibleGroups.forEach { group ->
                            val isSelected = group.id == selectedGroup?.id
                            GroupPill(
                                name = group.name,
                                isSelected = isSelected,
                                onClick = { viewModel.selectGroup(group.id) },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedGroup != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        // Group Header Section
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (isAdmin && isEditingGroupName) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    OutlinedTextField(
                                        value = groupNameInput,
                                        onValueChange = { groupNameInput = it },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("Group name") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GreenPrimary,
                                            focusedLabelColor = GreenPrimary,
                                            unfocusedBorderColor = GreenSecondary,
                                        ),
                                    )
                                    IconButton(
                                        onClick = {
                                            viewModel.renameSelectedGroup(groupNameInput)
                                            isEditingGroupName = false
                                        },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(GreenPrimary.copy(alpha = 0.12f)),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Save group name",
                                            tint = GreenPrimary,
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = selectedGroup.name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = GreenPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                    )
                                    if (isAdmin) {
                                        IconButton(
                                            onClick = {
                                                groupNameInput = selectedGroup.name
                                                isEditingGroupName = true
                                            },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(GreenPrimary.copy(alpha = 0.12f)),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit group name",
                                                tint = GreenPrimary,
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                InfoBadge(
                                    label = "Admin: ${selectedGroup.ownerUsername ?: "Unknown"}",
                                    isPrimary = true,
                                )
                                InfoBadge(
                                    label = "${selectedGroup.members.size} member${if (selectedGroup.members.size != 1) "s" else ""}",
                                    isPrimary = false,
                                )
                            }
                        }

                        Divider(color = GreenSecondary.copy(alpha = 0.2f), thickness = 1.dp)

                        // Members Section
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Members",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GreenPrimary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            if (uiState.isDetailLoading) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        color = GreenPrimary,
                                        strokeWidth = 2.5.dp
                                    )
                                }
                            } else if (selectedGroup.members.isEmpty()) {
                                Text(
                                    text = "No members yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GreenOnBackground.copy(alpha = 0.6f),
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    selectedGroup.members.forEach { member ->
                                        GroupMemberRow(
                                            member = member,
                                            isOwner = member.username.equals(selectedGroup.ownerUsername, ignoreCase = true),
                                            onKickRequested = if (
                                                isAdmin &&
                                                !member.username.equals(selectedGroup.ownerUsername, ignoreCase = true)
                                            ) {
                                                { memberToKick = member }
                                            } else {
                                                null
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        Divider(color = GreenSecondary.copy(alpha = 0.2f), thickness = 1.dp)

                        // Actions Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            BouncyAction(
                                text = "Leave",
                                container = GreenSecondary.copy(alpha = 0.12f),
                                content = GreenPrimary,
                                icon = Icons.AutoMirrored.Filled.ExitToApp,
                                modifier = Modifier.weight(1f),
                                onClick = { showLeaveConfirmation = true },
                            )
                            if (isAdmin) {
                                BouncyAction(
                                    text = "Delete",
                                    container = Color(0xFFFFEBEE),
                                    content = Color(0xFFC62828),
                                    icon = Icons.Default.Delete,
                                    modifier = Modifier.weight(1f),
                                    onClick = { showDeleteConfirmation = true },
                                )
                            }
                        }
                    }
                }

                if (!selectedGroup.isPersonal && isAdmin) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = "Group Join Code",
                                style = MaterialTheme.typography.titleMedium,
                                color = GreenPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = GreenPrimary.copy(alpha = 0.08f),
                                border = BorderStroke(1.5.dp, GreenPrimary.copy(alpha = 0.3f)),
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = selectedGroup.code ?: "Loading...",
                                        style = if (selectedGroup.code != null) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge,
                                        color = if (selectedGroup.code != null) GreenPrimary else Color.Gray,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = if (selectedGroup.code != null) 2.sp else 0.sp
                                    )
                                }
                            }

                            Text(
                                text = if (selectedGroup.code != null) "Share this code with others to join your cooking journey!" else "Fetching invite code...",
                                style = MaterialTheme.typography.bodySmall,
                                color = GreenOnBackground.copy(alpha = 0.6f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "No shared groups yet",
                            style = MaterialTheme.typography.titleLarge,
                            color = GreenPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create a new group or join with a code to start collaborating.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GreenOnBackground.copy(alpha = 0.75f),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(96.dp))
        }

        // Loading Overlay
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.15f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(44.dp),
                    color = GreenPrimary,
                    strokeWidth = 3.dp
                )
            }
        }
    }

    when (uiState.dialogMode) {
        GroupDialogMode.CHOICE -> {
            AlertDialog(
                onDismissRequest = viewModel::closeDialog,
                title = { Text("Group options") },
                text = { Text("Choose what you want to do next.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.openDialog(GroupDialogMode.JOIN) }) {
                        Text("Join group")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.openDialog(GroupDialogMode.CREATE) }) {
                        Text("Create group")
                    }
                },
            )
        }

        GroupDialogMode.JOIN -> {
            AlertDialog(
                onDismissRequest = viewModel::closeDialog,
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White,
                title = {
                    Text(
                        "Join group",
                        color = GreenPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Enter an invite code to join an existing group.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GreenOnBackground.copy(alpha = 0.75f),
                        )
                        OutlinedTextField(
                            value = uiState.joinCodeInput,
                            onValueChange = viewModel::setJoinCodeInput,
                            label = { Text("Group code") },
                            placeholder = { Text("e.g. A7K2P1") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                focusedLabelColor = GreenPrimary,
                                unfocusedBorderColor = GreenSecondary,
                            ),
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = viewModel::joinGroup,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text("Join") }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::closeDialog) { Text("Cancel") }
                },
            )
        }

        GroupDialogMode.CREATE -> {
            AlertDialog(
                onDismissRequest = viewModel::closeDialog,
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White,
                title = {
                    Text(
                        "Create group",
                        color = GreenPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Start a new group. You will be the initial admin.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GreenOnBackground.copy(alpha = 0.75f),
                        )
                        OutlinedTextField(
                            value = uiState.createNameInput,
                            onValueChange = viewModel::setCreateNameInput,
                            label = { Text("Group name") },
                            placeholder = { Text("e.g. Weekend Chefs") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                focusedLabelColor = GreenPrimary,
                                unfocusedBorderColor = GreenSecondary,
                            ),
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = viewModel::createGroup,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text("Create") }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::closeDialog) { Text("Cancel") }
                },
            )
        }

        null -> Unit
    }

    if (showLeaveConfirmation && selectedGroup != null) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirmation = false },
            title = { Text("Leave group?") },
            text = { Text("Are you sure you want to leave ${selectedGroup.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveConfirmation = false
                        viewModel.leaveSelectedGroup()
                    }
                ) { Text("Leave") }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirmation = false }) { Text("Cancel") }
            },
        )
    }

    if (showDeleteConfirmation && selectedGroup != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete group?") },
            text = { Text("Are you sure you want to delete ${selectedGroup.name}? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.deleteSelectedGroup()
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
            },
        )
    }

    memberToKick?.let { member ->
        AlertDialog(
            onDismissRequest = { memberToKick = null },
            title = { Text("Remove member?") },
            text = { Text("Are you sure you want to remove ${member.username} from this group?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.kickMemberFromSelectedGroup(member.username)
                        memberToKick = null
                    }
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { memberToKick = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun GroupPill(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) GreenPrimary else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isSelected) GreenPrimary else GreenPrimary.copy(alpha = 0.25f),
        ),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            color = if (isSelected) Color.White else GreenPrimary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun InfoBadge(
    label: String,
    isPrimary: Boolean,
) {
    val container = if (isPrimary) GreenPrimary.copy(alpha = 0.14f) else GreenSecondary.copy(alpha = 0.50f)
    val content = if (isPrimary) GreenPrimary else GreenOnBackground
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = container,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = content,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun GroupMemberRow(
    member: GroupMember,
    isOwner: Boolean,
    onKickRequested: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, GreenSecondary.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarBubble(seed = member.avatarSeed, username = member.username)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = member.username,
            style = MaterialTheme.typography.bodyLarge,
            color = GreenOnBackground,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        if (isOwner) {
            InfoBadge(label = "Admin", isPrimary = true)
        } else if (onKickRequested != null) {
            TextButton(onClick = onKickRequested) {
                Text("Kick", color = Color(0xFFC62828), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun AvatarBubble(
    seed: String,
    username: String,
) {
    val initials = username.toInitials()
    val isAlt = remember(seed) { seed.hashCode() % 2 == 0 }
    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = if (isAlt) GreenSecondary else GreenPrimary.copy(alpha = 0.20f),
        contentColor = if (isAlt) GreenOnBackground else GreenPrimary,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = initials, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BouncyAction(
    text: String,
    container: Color,
    content: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(stiffness = 450f, dampingRatio = 0.65f),
        label = "groupActionScale",
    )

    Surface(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = RoundedCornerShape(18.dp),
        color = container,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, color = content, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun String.toInitials(): String {
    val parts = trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    if (parts.isEmpty()) return "NA"
    if (parts.size == 1) return parts.first().take(2).uppercase()
    return "${parts.first().first()}${parts.last().first()}".uppercase()
}

@Preview(showBackground = true)
@Composable
private fun GroupsScreenPreview() {
    SnapChefTheme {
        GroupsScreen()
    }
}