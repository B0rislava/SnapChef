package com.snapchef.app.features.groups.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.snapchef.app.core.theme.GreenBackground
import com.snapchef.app.core.theme.GreenOnBackground
import com.snapchef.app.core.theme.GreenPrimary
import com.snapchef.app.core.theme.GreenSecondary
import com.snapchef.app.core.theme.SnapChefTheme
import kotlin.random.Random

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GroupsScreen(
    modifier: Modifier = Modifier,
) {
    val personalRecipes = remember {
        listOf(
            SharedRecipe(
                title = "Tomato Omelette",
                description = "Soft omelette with tomatoes and herbs.",
                ownerName = "You",
                missingItems = emptyList(),
            ),
            SharedRecipe(
                title = "Chicken Rice Bowl",
                description = "Rice bowl with chicken and green vegetables.",
                ownerName = "You",
                missingItems = emptyList(),
            ),
        )
    }

    var groups by remember {
        mutableStateOf(
            listOf(
                RecipeGroup(
                    id = "personal",
                    name = "Your recipes",
                    code = null,
                    recipes = personalRecipes,
                    isPersonal = true,
                ),
                RecipeGroup(
                    id = "g1",
                    name = "Flatmates",
                    code = "A7K2P1",
                    recipes = listOf(
                        SharedRecipe(
                            title = "Pasta Carbonara",
                            description = "Classic creamy pasta with bacon and parmesan.",
                            ownerName = "Anton",
                            missingItems = listOf("2 eggs"),
                        ),
                        SharedRecipe(
                            title = "Veggie Stir Fry",
                            description = "Fast stir fry with peppers and soy sauce.",
                            ownerName = "Mira",
                            missingItems = listOf("1 red pepper"),
                        ),
                    ),
                ),
            ),
        )
    }

    var selectedGroupId by remember { mutableStateOf("personal") }
    val selectedGroup = groups.firstOrNull { it.id == selectedGroupId } ?: groups.first()

    var expanded by remember { mutableStateOf(false) }
    var dialogMode by remember { mutableStateOf<GroupDialogMode?>(null) }
    var joinCodeInput by remember { mutableStateOf("") }
    var createNameInput by remember { mutableStateOf("") }
    var selectedRecipe by remember { mutableStateOf<SharedRecipe?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    if (selectedRecipe != null) {
        GroupRecipeDetailsScreen(
            recipe = selectedRecipe!!,
            onBack = { selectedRecipe = null },
            modifier = modifier,
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        GreenSecondary.copy(alpha = 0.55f),
                        GreenBackground,
                    ),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = 240.dp, y = (-40).dp)
                .clip(RoundedCornerShape(bottomStart = 160.dp))
                .background(GreenPrimary.copy(alpha = 0.10f)),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Recipes",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold,
                )

                IconButton(
                    onClick = { dialogMode = GroupDialogMode.CHOICE },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Join or create group",
                        tint = GreenPrimary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    value = selectedGroup.name,
                    onValueChange = {},
                    label = { Text("Select recipe source") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = GreenSecondary,
                        focusedLabelColor = GreenPrimary,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    ),
                    shape = RoundedCornerShape(18.dp),
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White, RoundedCornerShape(14.dp)),
                ) {
                    groups.forEach { group ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = group.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    if (!group.isPersonal && group.code != null) {
                                        Text(
                                            text = "Code: ${group.code}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GreenOnBackground.copy(alpha = 0.65f),
                                        )
                                    }
                                }
                            },
                            onClick = {
                                selectedGroupId = group.id
                                expanded = false
                            },
                        )
                    }
                }
            }

            if (!selectedGroup.isPersonal && selectedGroup.code != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Group code: ${selectedGroup.code}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GreenPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (infoMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = infoMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = GreenPrimary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (selectedGroup.isPersonal) "Your recipes" else "Group recipes",
                        style = MaterialTheme.typography.titleLarge,
                        color = GreenPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    selectedGroup.recipes.forEach { recipe ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clickable { selectedRecipe = recipe },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = GreenBackground),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = recipe.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GreenOnBackground,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Shared by ${recipe.ownerName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GreenOnBackground.copy(alpha = 0.7f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    when (dialogMode) {
        GroupDialogMode.CHOICE -> {
            AlertDialog(
                onDismissRequest = { dialogMode = null },
                title = { Text("Group options") },
                text = { Text("Choose what you want to do.") },
                confirmButton = {
                    TextButton(onClick = { dialogMode = GroupDialogMode.JOIN }) {
                        Text("Join group")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { dialogMode = GroupDialogMode.CREATE }) {
                        Text("Create group")
                    }
                },
            )
        }

        GroupDialogMode.JOIN -> {
            AlertDialog(
                onDismissRequest = { dialogMode = null },
                title = { Text("Join group") },
                text = {
                    OutlinedTextField(
                        value = joinCodeInput,
                        onValueChange = { joinCodeInput = it },
                        label = { Text("Enter group code") },
                        singleLine = true,
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val sanitizedCode = joinCodeInput.trim().uppercase()
                            if (sanitizedCode.length >= 4) {
                                val existing = groups.firstOrNull { it.code == sanitizedCode }
                                if (existing != null) {
                                    selectedGroupId = existing.id
                                    infoMessage = "You are already in this group."
                                } else {
                                    val newGroup = RecipeGroup(
                                        id = "joined_${Random.nextInt(1000, 9999)}",
                                        name = "Group ${sanitizedCode.take(4)}",
                                        code = sanitizedCode,
                                        recipes = listOf(
                                            SharedRecipe(
                                                title = "Shared Soup",
                                                description = "Group shared soup recipe.",
                                                ownerName = "Anton",
                                                missingItems = listOf("2 eggs"),
                                            ),
                                        ),
                                    )
                                    groups = groups + newGroup
                                    selectedGroupId = newGroup.id
                                    infoMessage = "Joined group ${newGroup.name}."
                                }
                            } else {
                                infoMessage = "Please enter a valid group code."
                            }
                            joinCodeInput = ""
                            dialogMode = null
                        },
                    ) { Text("Join") }
                },
                dismissButton = {
                    TextButton(onClick = { dialogMode = null }) { Text("Cancel") }
                },
            )
        }

        GroupDialogMode.CREATE -> {
            AlertDialog(
                onDismissRequest = { dialogMode = null },
                title = { Text("Create group") },
                text = {
                    OutlinedTextField(
                        value = createNameInput,
                        onValueChange = { createNameInput = it },
                        label = { Text("Group name") },
                        singleLine = true,
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val groupName = createNameInput.trim()
                            if (groupName.isNotBlank()) {
                                val code = generateGroupCode()
                                val created = RecipeGroup(
                                    id = "created_${Random.nextInt(1000, 9999)}",
                                    name = groupName,
                                    code = code,
                                    recipes = emptyList(),
                                )
                                groups = groups + created
                                selectedGroupId = created.id
                                infoMessage = "Group created. Code: $code"
                            } else {
                                infoMessage = "Group name cannot be empty."
                            }
                            createNameInput = ""
                            dialogMode = null
                        },
                    ) { Text("Create") }
                },
                dismissButton = {
                    TextButton(onClick = { dialogMode = null }) { Text("Cancel") }
                },
            )
        }

        null -> Unit
    }
}

@Composable
private fun GroupRecipeDetailsScreen(
    recipe: SharedRecipe,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        GreenSecondary.copy(alpha = 0.45f),
                        GreenBackground,
                    ),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = 250.dp, y = (-20).dp)
                .clip(RoundedCornerShape(bottomStart = 140.dp))
                .background(GreenPrimary.copy(alpha = 0.10f)),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(GreenPrimary.copy(alpha = 0.10f)),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GreenPrimary,
                    )
                }

                Spacer(modifier = Modifier.size(10.dp))

                Text(
                    text = "Recipe details",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = recipe.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = GreenOnBackground,
                            fontWeight = FontWeight.Bold,
                        )
                        Card(
                            shape = RoundedCornerShape(999.dp),
                            colors = CardDefaults.cardColors(containerColor = GreenSecondary),
                        ) {
                            Text(
                                text = "Shared",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = GreenOnBackground,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = GreenOnBackground,
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GreenBackground),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "${recipe.ownerName} needs:",
                                style = MaterialTheme.typography.titleSmall,
                                color = GreenPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (recipe.missingItems.isEmpty()) {
                                Text(
                                    text = "No missing ingredients.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GreenOnBackground,
                                )
                            } else {
                                recipe.missingItems.forEach { item ->
                                    Text(
                                        text = "- $item",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GreenOnBackground,
                                    )
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}

private fun generateGroupCode(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return buildString {
        repeat(6) {
            append(chars[Random.nextInt(chars.length)])
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupsScreenPreview() {
    SnapChefTheme {
        GroupsScreen()
    }
}

