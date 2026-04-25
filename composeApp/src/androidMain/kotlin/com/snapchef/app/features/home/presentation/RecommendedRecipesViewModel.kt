package com.snapchef.app.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapchef.app.core.auth.AuthManager
import com.snapchef.app.core.di.SnapChefServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecommendedRecipeItem(
    val title: String,
    val description: String,
    val instructions: List<String>,
    val ingredients: List<String>,
    val isQuick: Boolean,
    val catalogRecipeId: Int? = null,
)

data class RecommendedRecipesUiState(
    val recipes: List<RecommendedRecipeItem> = emptyList(),
    val openedRecipeIdx: Int? = null,
    val checkedIngredients: Map<String, Boolean> = emptyMap(),
    val infoMessage: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class RecommendedRecipesViewModel : ViewModel() {
    private val apiService = SnapChefServiceLocator.authApiService
    private val _uiState = MutableStateFlow(RecommendedRecipesUiState())
    val uiState: StateFlow<RecommendedRecipesUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (!AuthManager.isLoggedIn()) {
            _uiState.update { it.copy(recipes = emptyList(), isLoading = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = runCatching { apiService.getRecommendedRecipes(count = 6) }
            result.onSuccess { out ->
                val items = out.recipes.map { r ->
                    RecommendedRecipeItem(
                        title = r.title,
                        description = r.description.orEmpty(),
                        instructions = r.steps,
                        ingredients = r.ingredients,
                        isQuick = r.ingredients.size <= 6,
                        catalogRecipeId = r.id,
                    )
                }
                _uiState.update {
                    it.copy(recipes = items, isLoading = false, errorMessage = null)
                }
            }.onFailure { e ->
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Could not load recommendations.",
                    )
                }
            }
        }
    }

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
}
