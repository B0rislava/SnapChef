package com.snapchef.app.features.groups.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapchef.app.core.theme.GreenBackground
import com.snapchef.app.core.theme.GreenOnBackground
import com.snapchef.app.core.theme.GreenPrimary
import com.snapchef.app.core.theme.GreenSecondary
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snapchef.app.features.groups.presentation.RecipeStore

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RecipesScreen(
    modifier: Modifier = Modifier,
    viewModel: GroupsViewModel = viewModel(),
    onDetailsVisibilityChanged: (Boolean) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favoriteRecipeKeys by RecipeStore.favoriteRecipeKeys.collectAsStateWithLifecycle()
    val selectedGroup = uiState.groups.firstOrNull { it.id == uiState.selectedGroupId } ?: uiState.groups.first()
    val selectedRecipe = uiState.selectedRecipe
    val infoMessage = uiState.infoMessage
    var showFavoritesOnly by remember { mutableStateOf(false) }
    LaunchedEffect(selectedRecipe) {
        onDetailsVisibilityChanged(selectedRecipe != null)
    }

    if (selectedRecipe != null) {
        GroupRecipeDetailsScreen(
            recipe = selectedRecipe,
            onBack = viewModel::closeRecipeDetails,
            viewModel = viewModel,
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
                text = when {
                    selectedGroup.isPersonal && showFavoritesOnly -> "Hearted Recipes"
                    selectedGroup.isPersonal -> "All Saved Recipes"
                    else -> "${selectedGroup.name} Recipes"
                },
                style = MaterialTheme.typography.titleLarge,
                color = GreenPrimary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (selectedGroup.isPersonal) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (!showFavoritesOnly) GreenPrimary else GreenSecondary.copy(alpha = 0.35f),
                        modifier = Modifier.clickable { showFavoritesOnly = false }
                    ) {
                        Text(
                            text = "All",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = if (!showFavoritesOnly) Color.White else GreenOnBackground
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (showFavoritesOnly) GreenPrimary else GreenSecondary.copy(alpha = 0.35f),
                        modifier = Modifier.clickable { showFavoritesOnly = true }
                    ) {
                        Text(
                            text = "Favorites",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = if (showFavoritesOnly) Color.White else GreenOnBackground
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            val recipesToShow = if (selectedGroup.isPersonal) {
                if (showFavoritesOnly) {
                    selectedGroup.recipes.filter { r -> r.favoriteKey() in favoriteRecipeKeys }
                } else {
                    selectedGroup.recipes
                }
            } else {
                selectedGroup.recipes
            }

            if (recipesToShow.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (showFavoritesOnly) "No favorites yet." else "No recipes saved yet.",
                        color = GreenOnBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                recipesToShow.forEach { recipe ->
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                Text(
                                    text = recipe.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GreenOnBackground,
                                    fontWeight = FontWeight.Bold,
                                )
                                    val heartOn = recipe.favoriteKey() in favoriteRecipeKeys
                                    IconButton(onClick = { viewModel.toggleRecipeFavorite(recipe) }) {
                                        Icon(
                                            imageVector = if (heartOn) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Toggle favorite",
                                            tint = GreenPrimary
                                        )
                                    }
                                }
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
                                        text = when (recipe.ownerName) {
                                            "AI Suggestion" -> "AI suggested for your group"
                                            "You" -> "Saved by you"
                                            else -> "Shared by ${recipe.ownerName}"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (recipe.ownerName == "AI Suggestion") GreenPrimary else GreenOnBackground.copy(alpha = 0.7f),
                                        fontWeight = if (recipe.ownerName == "AI Suggestion") FontWeight.Bold else FontWeight.Normal
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
    viewModel: GroupsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inviteMessage by remember { mutableStateOf<String?>(null) }
    var showGroupSelection by remember { mutableStateOf(false) }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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
                    text = "Recipe Details",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Header & Actions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = recipe.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = GreenPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (recipe.ownerName == "AI Suggestion") GreenPrimary.copy(alpha=0.15f) else GreenSecondary.copy(alpha=0.3f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = when (recipe.ownerName) {
                                    "AI Suggestion" -> "AI Group Suggestion"
                                    "You" -> "Saved"
                                    else -> "Shared"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = GreenPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    if (recipe.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = recipe.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = GreenOnBackground.copy(alpha=0.8f),
                            lineHeight = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showGroupSelection = true },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Invite to cook together", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    inviteMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it, 
                            style = MaterialTheme.typography.labelMedium, 
                            color = GreenPrimary, 
                            modifier = Modifier.fillMaxWidth(), 
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Ingredients Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Ingredients",
                        style = MaterialTheme.typography.titleMedium,
                        color = GreenPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        recipe.availableItems.forEach { item ->
                            val parts = item.split(" (from ")
                            val itemName = parts[0]
                            val contributor = if (parts.size > 1) parts[1].removeSuffix(")") else null
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(GreenPrimary.copy(alpha = 0.1f))
                                    .border(1.dp, GreenPrimary.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = itemName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = GreenPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (contributor != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = GreenPrimary,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(16.dp)
                                        ) {
                                            Text(
                                                text = contributor,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp),
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        val spoiled = recipe.spoiledProducts()
                        recipe.missingItems.forEach { item ->
                            val isSpoiled = spoiled.any { s -> item.contains(s, ignoreCase = true) }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSpoiled) Color(0xFFFFEBEE) else Color(0xFFF5F5F5))
                                    .border(1.dp, if (isSpoiled) Color(0xFFE57373) else Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSpoiled) Color(0xFFC62828) else Color(0xFF9E9E9E),
                                    fontWeight = FontWeight.Medium,
                                    textDecoration = if (isSpoiled) TextDecoration.LineThrough else null
                                )
                            }
                        }
                    }
                }
            }

            // Instructions Card
            if (recipe.instructions.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Instructions",
                            style = MaterialTheme.typography.titleMedium,
                            color = GreenPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        recipe.instructions.forEachIndexed { index, step ->
                            Row(modifier = Modifier.padding(bottom = 16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(GreenPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GreenPrimary,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GreenOnBackground.copy(alpha = 0.85f),
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGroupSelection) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showGroupSelection = false },
            title = { 
                Text(
                    "Select Group", 
                    style = MaterialTheme.typography.titleLarge,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Which group would you like to invite to cook this recipe?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GreenOnBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    val groups = uiState.groups.filter { !it.isPersonal }
                    if (groups.isEmpty()) {
                        Text(
                            "You are not in any groups yet. Create or join a group first!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )
                    } else {
                        groups.forEach { group ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.shareRecipeToGroup(group.id, recipe)
                                        inviteMessage = "Recipe shared with ${group.name}!"
                                        showGroupSelection = false
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = GreenPrimary.copy(alpha = 0.05f)),
                                border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Group, null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = group.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = GreenOnBackground,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGroupSelection = false }) {
                    Text("Cancel", color = GreenPrimary)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp)
        )
    }
}



