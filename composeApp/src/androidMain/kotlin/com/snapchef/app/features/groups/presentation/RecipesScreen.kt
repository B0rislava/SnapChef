package com.snapchef.app.features.groups.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.snapchef.app.core.theme.GreenBackground
import com.snapchef.app.core.theme.GreenOnBackground
import com.snapchef.app.core.theme.GreenPrimary
import com.snapchef.app.core.theme.GreenSecondary
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RecipesScreen(
    modifier: Modifier = Modifier,
    viewModel: GroupsViewModel = viewModel(),
    onDetailsVisibilityChanged: (Boolean) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedGroup = uiState.groups.firstOrNull { it.id == uiState.selectedGroupId } ?: uiState.groups.first()
    val selectedRecipe = uiState.selectedRecipe
    val infoMessage = uiState.infoMessage
    LaunchedEffect(selectedRecipe) {
        onDetailsVisibilityChanged(selectedRecipe != null)
    }

    if (selectedRecipe != null) {
        GroupRecipeDetailsScreen(
            recipe = selectedRecipe,
            onBack = viewModel::closeRecipeDetails,
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.groups) { group ->
                    val isSelected = selectedGroup.id == group.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) GreenPrimary else Color.White)
                            .border(1.dp, if (isSelected) GreenPrimary else GreenPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .clickable { viewModel.selectGroup(group.id) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) Color.White else GreenPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (!selectedGroup.isPersonal && selectedGroup.code != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Group Code: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = GreenOnBackground.copy(alpha = 0.7f)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GreenPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = selectedGroup.code,
                            style = MaterialTheme.typography.labelMedium,
                            color = GreenPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (infoMessage != null) {
                Text(
                    text = infoMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = GreenPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (selectedGroup.isPersonal) "Your Saved Recipes" else "${selectedGroup.name} Recipes",
                style = MaterialTheme.typography.titleLarge,
                color = GreenPrimary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (selectedGroup.recipes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No recipes saved yet.", color = GreenOnBackground.copy(alpha = 0.6f))
                }
            } else {
                selectedGroup.recipes.forEach { recipe ->
                    val days = recipe.earliestDaysLeft()
                    val statusColor = when {
                        days == null -> GreenPrimary
                        days < 0 -> Color.Gray
                        days == 0 -> Color(0xFFC62828)
                        else -> GreenPrimary
                    }
                    val statusText = when {
                        days == null -> "Saved"
                        days < 0 -> "Expired ingredients"
                        days == 0 -> "Requires action today!"
                        else -> "Expires in $days days"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clickable { viewModel.openRecipe(recipe) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    ) {
                        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                            // Left colored border indicating status
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(6.dp)
                                    .background(statusColor)
                            )
                            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                                Text(
                                    text = recipe.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GreenOnBackground,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = GreenPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (recipe.ownerName == "You") "Saved by you" else "Shared by ${recipe.ownerName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GreenOnBackground.copy(alpha = 0.7f),
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(statusColor.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = statusColor,
                                        fontWeight = FontWeight.SemiBold
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

@Composable
private fun GroupRecipeDetailsScreen(
    recipe: SharedRecipe,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var inviteMessage by remember { mutableStateOf<String?>(null) }
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
                                text = if (recipe.ownerName == "You") "Saved" else "Shared",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = GreenOnBackground,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (recipe.ownerName != "You") {
                        Button(
                            onClick = { inviteMessage = "Invitation sent to ${recipe.ownerName}." },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text("Invite to cook together", color = Color.White)
                        }
                        inviteMessage?.let {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(it, style = MaterialTheme.typography.bodySmall, color = GreenPrimary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

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
                                text = "Instructions",
                                style = MaterialTheme.typography.titleSmall,
                                color = GreenPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (recipe.instructions.isEmpty()) {
                                Text(
                                    text = "No instructions available.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GreenOnBackground,
                                )
                            } else {
                                recipe.instructions.forEachIndexed { i, step ->
                                    Text(
                                        text = "${i + 1}. $step",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GreenOnBackground,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (recipe.perishableProducts.isNotEmpty()) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = GreenBackground),
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Perishable freshness",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = GreenPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                recipe.perishableProducts.forEach { product ->
                                    val daysLeft = product.daysLeft()
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (daysLeft < 0) Color.Gray else GreenOnBackground,
                                        textDecoration = if (daysLeft < 0) TextDecoration.LineThrough else TextDecoration.None,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Slider(
                                        value = product.freshness,
                                        onValueChange = {},
                                        valueRange = 0f..1f,
                                        enabled = false,
                                    )
                                    Text(
                                        text = when {
                                            daysLeft < 0 -> "Expired"
                                            daysLeft == 0 -> "Expires today"
                                            else -> "$daysLeft days left"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when {
                                            daysLeft < 0 -> Color.Gray
                                            daysLeft == 0 -> Color(0xFFC62828)
                                            else -> GreenPrimary
                                        },
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GreenBackground),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            if (recipe.availableItems.isNotEmpty()) {
                                Text(
                                    text = "${recipe.ownerName} has:",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = GreenPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                recipe.availableItems.forEach { item ->
                                    Text("- $item", style = MaterialTheme.typography.bodyMedium, color = GreenOnBackground)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
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
                                val spoiled = recipe.spoiledProducts()
                                recipe.missingItems.forEach { item ->
                                    Text(
                                        text = "- $item",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (spoiled.any { s -> item.contains(s, ignoreCase = true) }) Color.Gray else GreenOnBackground,
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



