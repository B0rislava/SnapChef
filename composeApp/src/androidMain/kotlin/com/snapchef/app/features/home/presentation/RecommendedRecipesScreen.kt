package com.snapchef.app.features.home.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import com.snapchef.app.core.theme.GreenOnBackground
import com.snapchef.app.core.theme.GreenPrimary
import com.snapchef.app.core.theme.GreenBackground
import com.snapchef.app.core.theme.GreenSecondary
import com.snapchef.app.features.groups.presentation.SharedRecipe

private data class RecommendedRecipeItem(
    val title: String,
    val description: String,
    val instructions: List<String>,
    val ingredients: List<String>,
    val isQuick: Boolean,
)

@Composable
private fun Pill(
    text: String,
    isQuick: Boolean,
) {
    // Matches the profile screen pill look (green primary vs secondary).
    val bg = if (isQuick) GreenPrimary else GreenSecondary
    val fg = if (isQuick) Color.White else GreenOnBackground
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bg,
        tonalElevation = 2.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = fg,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun RecipeCard(
    recipe: RecommendedRecipeItem,
    onPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(stiffness = 450f, dampingRatio = 0.65f),
        label = "recipeCardScale",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                onClick = onPress,
            ),
        shape = RoundedCornerShape(16.dp),
        color = GreenBackground,
        border = BorderStroke(1.5.dp, GreenSecondary),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Pill(
                text = if (recipe.isQuick) "Quick" else "Standard",
                isQuick = recipe.isQuick,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
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
        modifier = modifier
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
                .clickable(interactionSource = interactionSource, onClick = onClick)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = text, color = content, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun RecommendedRecipesScreen(
    onSaveRecipe: (SharedRecipe, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val recipes = remember {
        listOf(
            RecommendedRecipeItem(
                "Creamy Mushroom Pasta",
                "Quick creamy pasta for weeknights.",
                instructions = listOf(
                    "Boil pasta in salted water until al dente.",
                    "Cook mushrooms and garlic in olive oil until fragrant.",
                    "Stir in cream (or a dairy-free alternative) and season to taste.",
                    "Toss pasta with the sauce and finish with parmesan."
                ),
                ingredients = listOf("Pasta", "Mushrooms", "Garlic", "Cream", "Parmesan", "Olive oil"),
                isQuick = true,
            ),
            RecommendedRecipeItem(
                "Chicken Veggie Bowl",
                "Balanced protein bowl with fresh vegetables.",
                instructions = listOf(
                    "Cook rice (or use leftover rice) and keep warm.",
                    "Sear chicken until golden and cooked through.",
                    "Quick-sauté bell pepper and onions.",
                    "Combine everything with soy sauce and serve."
                ),
                ingredients = listOf("Chicken breast", "Rice", "Bell pepper", "Soy sauce", "Green onion", "Onion"),
                isQuick = true,
            ),
            RecommendedRecipeItem(
                "Spicy Chickpea Tacos",
                "Smoky, spicy chickpeas with crunchy toppings.",
                instructions = listOf(
                    "Sauté onion and garlic, then toast spices for 30 seconds.",
                    "Simmer chickpeas until saucy and flavorful.",
                    "Warm tortillas and assemble with toppings.",
                    "Finish with lime and a creamy drizzle."
                ),
                ingredients = listOf("Chickpeas", "Tortillas", "Onion", "Garlic", "Cumin", "Chili powder", "Lime", "Yogurt"),
                isQuick = false,
            ),
            RecommendedRecipeItem(
                "Lemon Herb Salmon",
                "Bright lemon-herb salmon with a buttery finish.",
                instructions = listOf(
                    "Preheat oven and season salmon with salt and pepper.",
                    "Bake until just flaky.",
                    "Mix butter (or olive oil) with lemon zest, juice, and herbs.",
                    "Pour over salmon and serve with greens."
                ),
                ingredients = listOf("Salmon", "Lemon", "Garlic", "Butter", "Dill", "Parsley", "Olive oil"),
                isQuick = true,
            ),
            RecommendedRecipeItem(
                "Tofu Stir-Fry",
                "Crispy tofu with colorful vegetables and a savory sauce.",
                instructions = listOf(
                    "Press tofu, then pan-sear until crisp.",
                    "Stir-fry vegetables on high heat.",
                    "Add sauce (soy + ginger + garlic) and toss until glossy.",
                    "Serve over rice or noodles."
                ),
                ingredients = listOf("Tofu", "Broccoli", "Carrot", "Soy sauce", "Ginger", "Garlic", "Cornstarch", "Sesame oil"),
                isQuick = true,
            ),
            RecommendedRecipeItem(
                "Greek Quinoa Salad",
                "Fresh quinoa salad with cucumber, feta, and herbs.",
                instructions = listOf(
                    "Cook quinoa and let it cool slightly.",
                    "Chop cucumber, tomato, and herbs.",
                    "Whisk olive oil with lemon juice and oregano.",
                    "Toss everything and finish with feta."
                ),
                ingredients = listOf("Quinoa", "Cucumber", "Tomato", "Feta", "Olive oil", "Lemon", "Oregano", "Red onion"),
                isQuick = false,
            ),
        )
    }
    var openedRecipeIdx by remember { mutableStateOf<Int?>(null) }
    val selected = openedRecipeIdx?.let { recipes[it] }
    val checked = remember(openedRecipeIdx) {
        mutableStateMapOf<String, Boolean>().apply {
            selected?.ingredients?.forEach { put(it, true) }
        }
    }
    var info by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(openedRecipeIdx) {
        // Clear feedback when switching between list and details.
        info = null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(GreenSecondary.copy(alpha = 0.55f), GreenBackground),
                )
            )
    ) {
        // Top circle accent (same vibe as profile).
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            if (selected == null) {
                // Header
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Recommended recipes",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = GreenPrimary,
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Recipes strip
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Pick something tasty",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Vertical list: each recipe lives in its own field (separate card).
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            recipes.forEachIndexed { index, recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    onPress = { openedRecipeIdx = index },
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            } else {
                // Details header
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { openedRecipeIdx = null },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GreenPrimary.copy(alpha = 0.10f)),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GreenPrimary,
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Recipe details",
                        color = GreenPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.titleSmall,
                                color = GreenPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = selected.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = GreenOnBackground,
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Ingredients",
                                style = MaterialTheme.typography.titleSmall,
                                color = GreenPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )

                            selected.ingredients.forEach { ingredient ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Checkbox(
                                        checked = checked[ingredient] ?: false,
                                        onCheckedChange = { checked[ingredient] = it },
                                    )
                                    Text(
                                        text = ingredient,
                                        color = GreenOnBackground,
                                        modifier = Modifier.padding(start = 6.dp),
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Instructions",
                                style = MaterialTheme.typography.titleSmall,
                                color = GreenPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            selected.instructions.forEachIndexed { i, step ->
                                Text(
                                    text = "${i + 1}. $step",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GreenOnBackground,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    BouncyAction(
                        modifier = Modifier.weight(1f),
                        text = "Save",
                        container = GreenPrimary,
                        content = Color.White,
                        onClick = {
                            val has = selected.ingredients.filter { checked[it] == true }
                            val needs = selected.ingredients.filter { checked[it] != true }
                            onSaveRecipe(
                                SharedRecipe(
                                    title = selected.title,
                                    description = selected.description,
                                    ownerName = "You",
                                    missingItems = needs,
                                    availableItems = has,
                                    instructions = selected.instructions,
                                ),
                                false,
                            )
                            info = "Saved."
                        },
                    )
                    BouncyAction(
                        modifier = Modifier.weight(1f),
                        text = "Share",
                        container = GreenSecondary.copy(alpha = 0.5f),
                        content = GreenPrimary,
                        onClick = {
                            val has = selected.ingredients.filter { checked[it] == true }
                            val needs = selected.ingredients.filter { checked[it] != true }
                            onSaveRecipe(
                                SharedRecipe(
                                    title = selected.title,
                                    description = selected.description,
                                    ownerName = "You",
                                    missingItems = needs,
                                    availableItems = has,
                                    instructions = selected.instructions,
                                ),
                                true,
                            )
                            info = "Shared to your group."
                        },
                    )
                }

                info?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = GreenPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

