package com.snapchef.app.features.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.snapchef.app.core.theme.GreenBackground
import com.snapchef.app.core.theme.GreenOnBackground
import com.snapchef.app.core.theme.GreenPrimary
import com.snapchef.app.core.theme.GreenSecondary
import com.snapchef.app.features.groups.presentation.PerishableProduct
import com.snapchef.app.features.groups.presentation.SharedRecipe

@Composable
fun RecipeResultsScreen(
    ingredients: List<String>,
    onBack: () -> Unit,
    onSaveRecipe: (SharedRecipe, Boolean) -> Unit,
) {
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
    var actionMessage by remember { mutableStateOf<String?>(null) }
    val suggestedRecipes = remember(ingredients) {
        listOf(
            "Smart Omelette",
            "Quick Bowl",
            "Zero-Waste Stir Fry",
        ).map { title ->
            SharedRecipe(
                title = title,
                description = "Generated from your detected products: ${ingredients.joinToString()}",
                ownerName = "You",
                missingItems = emptyList(),
                instructions = generateRecipeInstructions(ingredients),
            )
        }
    }
    var selectedRecipeIndex by remember { mutableStateOf<Int?>(null) }
    var previewRecipeIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenSecondary.copy(alpha = 0.3f))
            .systemBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GreenPrimary.copy(alpha = 0.10f))
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

        Spacer(modifier = Modifier.height(24.dp))

        // Info card showing what we used
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = GreenBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Generated using:",
                    style = MaterialTheme.typography.titleMedium,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                val ingredientText = ingredients.joinToString(separator = " • ")
                Text(
                    text = ingredientText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GreenOnBackground.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (perishableIngredients.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Before saving: set freshness",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Move the slider for perishable products.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GreenOnBackground.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    perishableIngredients.forEach { ingredient ->
                        val value = freshness[ingredient] ?: 1f
                        val daysLeft = kotlin.math.ceil(14f * value).toInt() - 1
                        Text(
                            text = ingredient,
                            style = MaterialTheme.typography.bodyMedium,
                            color = GreenOnBackground,
                            fontWeight = FontWeight.SemiBold
                        )
                        Slider(
                            value = value,
                            onValueChange = { freshness[ingredient] = it },
                            valueRange = 0f..1f
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
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                val index = selectedRecipeIndex ?: return@Button
                                val selected = suggestedRecipes[index].copy(
                                    perishableProducts = perishableIngredients.map { item ->
                                        PerishableProduct(
                                            name = item,
                                            maxFreshDays = 14,
                                            freshness = freshness[item] ?: 1f,
                                        )
                                    },
                                )
                                onSaveRecipe(selected, false)
                                actionMessage = "Recipe saved."
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            enabled = selectedRecipeIndex != null
                        ) { Text("Save", color = Color.White) }

                        OutlinedButton(
                            onClick = {
                                val index = selectedRecipeIndex ?: return@OutlinedButton
                                val selected = suggestedRecipes[index].copy(
                                    perishableProducts = perishableIngredients.map { item ->
                                        PerishableProduct(
                                            name = item,
                                            maxFreshDays = 14,
                                            freshness = freshness[item] ?: 1f,
                                        )
                                    },
                                )
                                onSaveRecipe(selected, true)
                                actionMessage = "Recipe shared."
                            },
                            shape = RoundedCornerShape(20.dp),
                            enabled = selectedRecipeIndex != null
                        ) { Text("Share") }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        actionMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = GreenPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
            text = "Suggested Recipes",
            style = MaterialTheme.typography.titleLarge,
            color = GreenPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Placeholder lazy column for future recipe results
         LazyColumn(
             modifier = Modifier.fillMaxSize(),
             verticalArrangement = Arrangement.spacedBy(16.dp)
         ) {
             items(suggestedRecipes.size) { index ->
                 val isSelected = selectedRecipeIndex == index
                 Card(
                     modifier = Modifier
                         .fillMaxWidth()
                         .height(140.dp)
                         .clickable { previewRecipeIndex = index },
                     shape = RoundedCornerShape(24.dp),
                     colors = CardDefaults.cardColors(
                         containerColor = if (isSelected) GreenSecondary.copy(alpha = 0.45f) else Color.White
                     ),
                     elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                 ) {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                         Text(
                             text = if (isSelected) {
                                 "${suggestedRecipes[index].title}\nSelected"
                             } else {
                                 "${suggestedRecipes[index].title}\nTap to view instructions"
                             },
                             color = GreenPrimary,
                             fontWeight = FontWeight.Bold,
                             textAlign = androidx.compose.ui.text.style.TextAlign.Center
                         )
                     }
                 }
             }
         }
    }

    previewRecipeIndex?.let { index ->
        val recipe = suggestedRecipes[index]
        AlertDialog(
            onDismissRequest = { previewRecipeIndex = null },
            title = { Text(recipe.title) },
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
                Button(
                    onClick = {
                        selectedRecipeIndex = index
                        previewRecipeIndex = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Select this recipe", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { previewRecipeIndex = null }) {
                    Text("Close")
                }
            },
        )
    }
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
