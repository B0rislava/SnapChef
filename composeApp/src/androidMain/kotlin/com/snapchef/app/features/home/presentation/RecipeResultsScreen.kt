package com.snapchef.app.features.home.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapchef.app.core.theme.GreenBackground
import com.snapchef.app.core.theme.GreenOnBackground
import com.snapchef.app.core.theme.GreenPrimary
import com.snapchef.app.core.theme.GreenSecondary
import com.snapchef.app.features.groups.presentation.SharedRecipe

private data class GroupRecipeContribution(
    val name: String,
    val items: String,
    val color: Color,
)

private data class GroupRecipeResult(
    val title: String,
    val contributors: List<GroupRecipeContribution>,
    val missingItems: List<String>,
    val description: String,
)

@Composable
fun RecipeResultsScreen(
    ingredients: List<String>,
    onBack: () -> Unit,
    onSaveRecipe: (SharedRecipe, Boolean) -> Unit,
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    val perishableKeywords = remember {
        setOf("egg", "eggs", "milk", "cheese", "yogurt", "chicken", "fish", "meat", "mushroom", "mushrooms")
    }
    val perishableIngredients = remember(ingredients) {
        ingredients.filter { name ->
            perishableKeywords.any { key -> name.contains(key, ignoreCase = true) }
        }
    }
    val freshness = remember(perishableIngredients) {
        mutableStateMapOf<String, Float>().apply {
            perishableIngredients.forEach { put(it, 1f) }
        }
    }
    
    val soloRecipes = remember(ingredients) {
        listOf("Smart Omelette", "Quick Bowl", "Zero-Waste Stir Fry").map { title ->
            SharedRecipe(
                title = title,
                description = "Generated from your detected products: ${ingredients.joinToString()}",
                ownerName = "You",
                missingItems = emptyList(),
                instructions = generateRecipeInstructions(ingredients),
            )
        }
    }

    val groupRecipes = remember(ingredients) {
        listOf(
            GroupRecipeResult(
                title = "Hero's Feast",
                contributors = listOf(
                    GroupRecipeContribution("You", ingredients.joinToString(), GreenPrimary)
                ),
                missingItems = listOf("Cheese", "Milk"),
                description = "You started this! If someone adds milk and cheese, we can make it a feast."
            ),
            GroupRecipeResult(
                title = "Big Salad Bowl",
                contributors = listOf(
                    GroupRecipeContribution("You", ingredients.take(1).joinToString(), GreenPrimary)
                ),
                missingItems = listOf("Cucumbers", "Olive oil", "Olives"),
                description = "Just a few more items needed. Share with your group to find them."
            )
        )
    }

    var actionMessage by remember { mutableStateOf<String?>(null) }
    var previewRecipeIndex by remember { mutableStateOf<Int?>(null) }
    var previewGroupRecipeIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(GreenSecondary.copy(alpha = 0.2f), GreenBackground)
                )
            )
            .systemBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = GreenPrimary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Recipe Magic",
                style = MaterialTheme.typography.headlineMedium,
                color = GreenPrimary,
                fontWeight = FontWeight.ExtraBold
            )
        }

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = GreenPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = GreenPrimary
                )
            },
            divider = {},
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Solo", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Person, null) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("With Group", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Group, null) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(
            targetState = selectedTabIndex,
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            },
            label = "tab_content",
            modifier = Modifier.weight(1f)
        ) { tab ->
            when (tab) {
                0 -> SoloRecipeList(
                    ingredients = ingredients,
                    recipes = soloRecipes,
                    perishableIngredients = perishableIngredients,
                    freshness = freshness,
                    onPreview = { previewRecipeIndex = it },
                    onActionMessage = { actionMessage = it },
                    onSave = onSaveRecipe
                )
                1 -> GroupRecipeList(
                    recipes = groupRecipes,
                    onPreview = { previewGroupRecipeIndex = it }
                )
            }
        }

        actionMessage?.let {
            Text(
                text = it,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                textAlign = TextAlign.Center,
                color = GreenPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }


    previewRecipeIndex?.let { index ->
        val recipe = soloRecipes[index]
        RecipeInstructionsDialog(
            recipe = recipe,
            onDismiss = { previewRecipeIndex = null }
        )
    }

    previewGroupRecipeIndex?.let { index ->
        val recipe = groupRecipes[index]
        AlertDialog(
            onDismissRequest = { previewGroupRecipeIndex = null },
            title = { Text(recipe.title) },
            text = { Text(recipe.description) },
            confirmButton = {
                TextButton(onClick = { previewGroupRecipeIndex = null }) {
                    Text("Super!", color = GreenPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun SoloRecipeList(
    ingredients: List<String>,
    recipes: List<SharedRecipe>,
    perishableIngredients: List<String>,
    freshness: Map<String, Float>,
    onPreview: (Int) -> Unit,
    onActionMessage: (String) -> Unit,
    onSave: (SharedRecipe, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (ingredients.isNotEmpty()) {
            item {
                Text(
                    text = "Detected Ingredients",
                    style = MaterialTheme.typography.titleSmall,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ingredients.forEach { item ->
                        val isPerishable = perishableIngredients.contains(item)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isPerishable) GreenPrimary.copy(alpha = 0.15f) else Color.White)
                                .border(1.dp, if (isPerishable) GreenPrimary else Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isPerishable) GreenPrimary else GreenOnBackground.copy(alpha = 0.8f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            Text(
                text = "Your Magic Recipes",
                style = MaterialTheme.typography.titleLarge,
                color = GreenPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        items(recipes.indices.toList()) { index ->
            val recipe = recipes[index]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPreview(index) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(GreenPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✨",
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = recipe.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = GreenOnBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Uses: ${ingredients.take(3).joinToString()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = GreenPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { onSave(recipe, false); onActionMessage("Recipe saved!") },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text("Save Recipe", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupRecipeList(
    recipes: List<GroupRecipeResult>,
    onPreview: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GreenPrimary.copy(alpha = 0.1f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "You're the first! ✨",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Share these ideas with your group so they can see what they can add from their fridges.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GreenPrimary.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "Social Cooking Ideas",
                style = MaterialTheme.typography.titleLarge,
                color = GreenPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        items(recipes.indices.toList()) { index ->
            val recipe = recipes[index]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPreview(index) },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = GreenOnBackground
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Contributor avatars
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy((-12).dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            recipe.contributors.forEach { contributor ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(contributor.color)
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = contributor.name.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = "${recipe.contributors.size} contributor${if (recipe.contributors.size == 1) "" else "s"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = GreenPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Ingredients",
                        style = MaterialTheme.typography.titleSmall,
                        color = GreenOnBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val availableItems = recipe.contributors.flatMap { 
                            it.items.split(", ") 
                        }.filter { it.isNotBlank() }

                        availableItems.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(GreenPrimary.copy(alpha = 0.1f))
                                    .border(1.dp, GreenPrimary.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GreenPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        recipe.missingItems.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF5F5F5))
                                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF9E9E9E),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeInstructionsDialog(
    recipe: SharedRecipe,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(recipe.title, color = GreenPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Instructions",
                    style = MaterialTheme.typography.titleSmall,
                    color = GreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                recipe.instructions.forEach { step ->
                    Text(
                        text = "• $step",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GreenOnBackground
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it!", color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

private fun generateRecipeInstructions(ingredients: List<String>): List<String> {
    val base = listOf(
        "Wash and prep all ingredients.",
        "Heat a pan on medium heat with a small amount of oil.",
        "Add the hardest ingredients first and cook for 3-4 minutes.",
        "Add the remaining ingredients and season to taste.",
        "Cook until texture looks right, then serve warm.",
    )
    return if (ingredients.isEmpty()) base else {
        base.toMutableList().apply {
            add(1, "Use these products first: ${ingredients.take(3).joinToString()}.")
        }
    }
}
