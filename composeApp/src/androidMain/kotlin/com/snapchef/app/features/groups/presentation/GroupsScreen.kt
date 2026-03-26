package com.snapchef.app.features.groups.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snapchef.app.core.theme.GreenBackground
import com.snapchef.app.core.theme.GreenPrimary
import com.snapchef.app.core.theme.GreenSecondary
import com.snapchef.app.core.theme.SnapChefTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    modifier: Modifier = Modifier,
    viewModel: GroupsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(GreenSecondary.copy(alpha = 0.55f), GreenBackground)
                )
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Groups",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold,
                )

                IconButton(
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
    }

    when (uiState.dialogMode) {
        GroupDialogMode.CHOICE -> {
            AlertDialog(
                onDismissRequest = viewModel::closeDialog,
                title = { Text("Group options") },
                text = { Text("Choose what you want to do.") },
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
                title = { Text("Join group") },
                text = {
                    OutlinedTextField(
                        value = uiState.joinCodeInput,
                        onValueChange = viewModel::setJoinCodeInput,
                        label = { Text("Enter group code") },
                        singleLine = true,
                    )
                },
                confirmButton = {
                    Button(
                        onClick = viewModel::joinGroup,
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
                title = { Text("Create group") },
                text = {
                    OutlinedTextField(
                        value = uiState.createNameInput,
                        onValueChange = viewModel::setCreateNameInput,
                        label = { Text("Group name") },
                        singleLine = true,
                    )
                },
                confirmButton = {
                    Button(
                        onClick = viewModel::createGroup,
                    ) { Text("Create") }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::closeDialog) { Text("Cancel") }
                },
            )
        }

        null -> Unit
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupsScreenPreview() {
    SnapChefTheme {
        GroupsScreen()
    }
}
