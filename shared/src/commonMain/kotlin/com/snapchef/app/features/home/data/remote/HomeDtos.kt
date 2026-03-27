package com.snapchef.app.features.home.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Scanning a Fridge Image ---

@Serializable
data class ScanItemOut(
    val name: String,
    val quantity: Float? = null,
    val unit: String? = null,
    val confidence: Float? = null,
    @SerialName("expires_at") val expiresAt: String? = null
)

@Serializable
data class ImageScanResponse(
    val items: List<ScanItemOut> = emptyList(),
    val raw: String? = null
)

// --- Suggesting Recipes ---

@Serializable
data class RecipeSuggestRequest(
    val items: List<String>
)

@Serializable
data class RecipeOut(
    val id: Int,
    val title: String,
    val description: String? = null,
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val starred: Boolean = false
)
