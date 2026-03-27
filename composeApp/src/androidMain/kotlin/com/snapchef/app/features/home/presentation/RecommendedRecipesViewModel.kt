package com.snapchef.app.features.home.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RecommendedRecipeItem(
    val title: String,
    val description: String,
    val instructions: List<String>,
    val ingredients: List<String>,
    val isQuick: Boolean,
)

data class RecommendedRecipesUiState(
    val recipes: List<RecommendedRecipeItem> = emptyList(),
    val openedRecipeIdx: Int? = null,
    val checkedIngredients: Map<String, Boolean> = emptyMap(),
    val infoMessage: String? = null,
)

class RecommendedRecipesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        RecommendedRecipesUiState(
            recipes = sampleRecipes(),
        )
    )
    val uiState: StateFlow<RecommendedRecipesUiState> = _uiState.asStateFlow()

    fun openRecipe(index: Int) {
        val recipe = _uiState.value.recipes.getOrNull(index) ?: return
        _uiState.update {
            it.copy(
                openedRecipeIdx = index,
                checkedIngredients = recipe.ingredients.associateWith { true },
                infoMessage = null,
            )
        }
    }

    fun closeRecipe() {
        _uiState.update { it.copy(openedRecipeIdx = null, checkedIngredients = emptyMap(), infoMessage = null) }
    }

    fun toggleIngredient(ingredient: String, checked: Boolean) {
        _uiState.update {
            it.copy(
                checkedIngredients = it.checkedIngredients.toMutableMap().apply {
                    put(ingredient, checked)
                }
            )
        }
    }

    fun setInfoMessage(value: String?) {
        _uiState.update { it.copy(infoMessage = value) }
    }

    private fun sampleRecipes(): List<RecommendedRecipeItem> {
        return listOf(
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
                    "Quick-saute bell pepper and onions.",
                    "Combine everything with soy sauce and serve."
                ),
                ingredients = listOf("Chicken breast", "Rice", "Bell pepper", "Soy sauce", "Green onion", "Onion"),
                isQuick = true,
            ),
            RecommendedRecipeItem(
                "Spicy Chickpea Tacos",
                "Smoky, spicy chickpeas with crunchy toppings.",
                instructions = listOf(
                    "Saute onion and garlic, then toast spices for 30 seconds.",
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
}

