package com.snapchef.app.features.home.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.snapchef.app.core.theme.GreenBackground
import com.snapchef.app.core.theme.GreenOnBackground
import com.snapchef.app.core.theme.GreenPrimary
import com.snapchef.app.core.theme.GreenSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGenerateRecipes: (List<String>) -> Unit
) {
    var showModal by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var ingredients by remember { mutableStateOf(listOf<String>()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(GreenSecondary.copy(alpha = 0.45f), GreenBackground)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Big Animated Camera Button
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(GreenPrimary.copy(alpha = 0.15f))
                    .clickable {
                        coroutineScope.launch {
                            ingredients = emptyList()
                            isAnalyzing = true
                            showModal = true
                            
                            // Simulate AI recognition delay
                            delay(1500)
                            ingredients = listOf("Tomatoes", "Eggs", "Cheese", "Onion")
                            isAnalyzing = false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = "Snap Ingredients",
                        modifier = Modifier.size(80.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Snap your ingredients",
                style = MaterialTheme.typography.headlineMedium,
                color = GreenPrimary,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Take a photo of the food in your kitchen.\nWe'll recognize what you have and generate delicious recipes instantly.",
                style = MaterialTheme.typography.bodyLarge,
                color = GreenOnBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        // Recognition Modal Bottom Sheet
        if (showModal) {
            ModalBottomSheet(
                onDismissRequest = { showModal = false },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle(color = GreenSecondary) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isAnalyzing) "Analyzing Photo..." else "Ingredients Found",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedContent(
                        targetState = isAnalyzing,
                        label = "analysis state",
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                        }
                    ) { analyzing ->
                        if (analyzing) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = GreenPrimary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Identifying food items with AI...", color = GreenOnBackground.copy(alpha = 0.6f))
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Confirm the items below, or remove any mistakes before generating recipes.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GreenOnBackground.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 280.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(ingredients) { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(GreenBackground)
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Rounded.CheckCircle,
                                                    contentDescription = null,
                                                    tint = GreenPrimary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = item,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Medium,
                                                    color = GreenOnBackground
                                                )
                                            }
                                            
                                            IconButton(
                                                onClick = {
                                                    ingredients = ingredients.filter { it != item }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Delete,
                                                    contentDescription = "Remove $item",
                                                    tint = GreenSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                var newIngredient by remember { mutableStateOf("") }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 24.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newIngredient,
                                        onValueChange = { newIngredient = it },
                                        modifier = Modifier.weight(1f).height(56.dp),
                                        placeholder = { Text("Add missing ingredient") },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GreenPrimary,
                                            unfocusedBorderColor = GreenSecondary.copy(alpha = 0.4f),
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    IconButton(
                                        onClick = {
                                            if (newIngredient.isNotBlank()) {
                                                ingredients = ingredients + newIngredient.trim()
                                                newIngredient = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(GreenPrimary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = "Add",
                                            tint = Color.White
                                        )
                                    }
                                }
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            sheetState.hide()
                                            showModal = false
                                            onGenerateRecipes(ingredients)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(28.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                    enabled = ingredients.isNotEmpty()
                                ) {
                                    Text(
                                        text = "Generate Recipes",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White
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
