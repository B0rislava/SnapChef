package com.snapchef.app.features.groups.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object RecipeStore {
    private val _personalRecipes = MutableStateFlow<List<SharedRecipe>>(emptyList())
    val personalRecipes: StateFlow<List<SharedRecipe>> = _personalRecipes.asStateFlow()
    private val _sharedRecipes = MutableStateFlow<List<SharedRecipe>>(emptyList())
    val sharedRecipes: StateFlow<List<SharedRecipe>> = _sharedRecipes.asStateFlow()

    fun addPersonalRecipe(recipe: SharedRecipe) {
        _personalRecipes.update { listOf(recipe) + it }
    }

    fun addSharedRecipe(recipe: SharedRecipe) {
        _sharedRecipes.update { listOf(recipe) + it }
    }
}

