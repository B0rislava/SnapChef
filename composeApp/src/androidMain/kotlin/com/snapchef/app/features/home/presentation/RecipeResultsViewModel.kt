package com.snapchef.app.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapchef.app.core.di.SnapChefServiceLocator
import com.snapchef.app.features.groups.presentation.SharedRecipe
import com.snapchef.app.features.home.data.remote.resolvedTitle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeResultsUiState(
    val isLoading: Boolean = false,
    val recipes: List<SharedRecipe> = emptyList(),
    val errorMessage: String? = null
)

class RecipeResultsViewModel : ViewModel() {
    private val apiService = SnapChefServiceLocator.homeApiService
    private val _uiState = MutableStateFlow(RecipeResultsUiState())
    val uiState: StateFlow<RecipeResultsUiState> = _uiState.asStateFlow()

    fun loadRecipesForSession(sessionId: Int) {
        if (_uiState.value.recipes.isNotEmpty() || _uiState.value.isLoading) return
        
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val response = apiService.suggestRecipes(sessionId)
                val mappedRecipes = response.recipes.map { backendRecipe ->
                    SharedRecipe(
                        title = backendRecipe.resolvedTitle(),
                        description = "Ready in ${backendRecipe.minutes ?: "?"} mins",
                        ownerName = "AI Magic",
                        instructions = backendRecipe.steps,
                        availableItems = backendRecipe.uses,
                        missingItems = backendRecipe.extra,
                        sessionRecipeId = backendRecipe.id,
                    )
                }
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        recipes = mappedRecipes
                    ) 
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Failed to generate recipes. Please try again."
                    ) 
                }
            }
        }
    }
}

