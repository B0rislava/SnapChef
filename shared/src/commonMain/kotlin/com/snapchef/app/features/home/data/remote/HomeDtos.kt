package com.snapchef.app.features.home.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class IngredientAnalysisRequest(
    val imageBase64: String
)

@Serializable
data class IngredientAnalysisResponse(
    val ingredients: List<String>
)

@Serializable
data class RecipeGenerationRequest(
    val ingredients: List<String>
)

@Serializable
data class Recipe(
    val id: String,
    val title: String,
    val instructions: List<String>,
    val imageUrl: String? = null
)

@Serializable
data class RecipeResponse(
    val recipes: List<Recipe>
)
