package com.snapchef.app.features.home.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snapchef.app.core.theme.GreenBackground
import com.snapchef.app.core.theme.GreenOnBackground
import com.snapchef.app.core.theme.GreenPrimary
import com.snapchef.app.core.theme.GreenSecondary
import com.snapchef.app.features.groups.presentation.SharedRecipe
import kotlinx.coroutines.delay

@Composable
private fun Pill(
    text: String,
    isQuick: Boolean,
) {
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
private fun InfoChip(
    icon: ImageVector,
    text: String,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = GreenSecondary.copy(alpha = 0.45f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = GreenOnBackground,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Pill(
                    text = if (recipe.isQuick) "Quick pick" else "Chef choice",
                    isQuick = recipe.isQuick,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (recipe.isQuick) "15-20 min" else "30-40 min",
                        style = MaterialTheme.typography.labelMedium,
                        color = GreenOnBackground.copy(alpha = 0.75f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GreenOnBackground,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyMedium,
                color = GreenOnBackground.copy(alpha = 0.72f),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${recipe.ingredients.size} ingredients",
                    style = MaterialTheme.typography.labelLarge,
                    color = GreenPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }
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
    onDetailsVisibilityChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: RecommendedRecipesViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recipes = uiState.recipes
    val selected = uiState.openedRecipeIdx?.let { recipes.getOrNull(it) }

    LaunchedEffect(selected) {
        onDetailsVisibilityChanged(selected != null)
    }
    LaunchedEffect(uiState.infoMessage) {
        if (uiState.infoMessage != null) {
            delay(2500)
            viewModel.setInfoMessage(null)
        }
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

            if (selected == null) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            text = "Recommended recipes",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = GreenPrimary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Fresh picks personalized for your kitchen.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GreenOnBackground.copy(alpha = 0.75f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenPrimary),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = "Today's picks",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.9f),
                            )
                            Text(
                                text = "${recipes.size} curated recipes",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color.White,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Choose your next meal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            recipes.forEachIndexed { index, recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    onPress = { viewModel.openRecipe(index) },
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(96.dp))
            } else {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = viewModel::closeRecipe,
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
                        Text(
                            text = selected.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = GreenPrimary,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Pill(
                                text = if (selected.isQuick) "Quick pick" else "Chef choice",
                                isQuick = selected.isQuick,
                            )
                            InfoChip(
                                icon = Icons.Default.Checklist,
                                text = "${selected.ingredients.size} items",
                            )
                        }

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

                        Surface(
                            color = GreenBackground,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, GreenSecondary.copy(alpha = 0.6f)),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
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
                                            checked = uiState.checkedIngredients[ingredient] ?: false,
                                            onCheckedChange = { viewModel.toggleIngredient(ingredient, it) },
                                        )
                                        Text(
                                            text = ingredient,
                                            color = GreenOnBackground,
                                            modifier = Modifier.padding(start = 6.dp),
                                        )
                                    }
                                }
                            }
                        }

                        Surface(
                            color = GreenBackground,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, GreenSecondary.copy(alpha = 0.6f)),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    text = "Instructions",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = GreenPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                selected.instructions.forEachIndexed { i, step ->
                                    Row(verticalAlignment = Alignment.Top) {
                                        Surface(
                                            shape = CircleShape,
                                            color = GreenPrimary.copy(alpha = 0.14f),
                                            modifier = Modifier.size(22.dp),
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${i + 1}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = GreenPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = step,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = GreenOnBackground,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }

                        val checkedCount = selected.ingredients.count { uiState.checkedIngredients[it] == true }
                        Text(
                            text = "You have $checkedCount of ${selected.ingredients.size} ingredients ready.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GreenOnBackground.copy(alpha = 0.85f),
                        )
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
                            val has = selected.ingredients.filter { uiState.checkedIngredients[it] == true }
                            val needs = selected.ingredients.filter { uiState.checkedIngredients[it] != true }
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
                            viewModel.setInfoMessage("Saved.")
                        },
                    )
                    BouncyAction(
                        modifier = Modifier.weight(1f),
                        text = "Share",
                        container = GreenSecondary.copy(alpha = 0.5f),
                        content = GreenPrimary,
                        onClick = {
                            val has = selected.ingredients.filter { uiState.checkedIngredients[it] == true }
                            val needs = selected.ingredients.filter { uiState.checkedIngredients[it] != true }
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
                            viewModel.setInfoMessage("Shared to your group.")
                        },
                    )
                }

                uiState.infoMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = GreenPrimary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(
                            text = it,
                            color = GreenPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }
}
