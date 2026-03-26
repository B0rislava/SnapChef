package com.snapchef.app.features.groups.presentation

data class RecipeGroup(
    val id: String,
    val name: String,
    val code: String?,
    val recipes: List<SharedRecipe>,
    val isPersonal: Boolean = false,
)

