package com.snapchef.app.features.home.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// --- Scanning a Fridge Image ---

@Serializable
data class ScanItemOut(
    val id: Int,
    val name: String,
    val freshness: Int? = null,
    val qty: String? = null,
    val unit: String? = null,
    val confidence: Float? = null,
    val source: String? = null,
    val alert: String? = null
)

@Serializable
data class ScanImageOut(
    val id: Int,
    val mime: String
)

@Serializable
data class ScanSessionResponse(
    val id: Int = 0,
    val status: String = "",
    val status_msg: String? = null,
    val images: List<ScanImageOut> = emptyList(),
    val items: List<ScanItemOut> = emptyList()
)

// --- Suggesting Recipes ---

@Serializable
data class GroqRecipeSuggestResponse(
    val recipes: List<SessionRecipeOut>
)

@Serializable
data class SessionRecipeOut(
    val id: Int,
    @SerialName("session_id") val sessionId: Int = 0,
    val name: String,
    val uses: List<String> = emptyList(),
    val extra: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val minutes: Int? = null,
    val rating: Int? = null
)

@Serializable
data class LibraryRecipeOut(
    val id: Int = 0,
    val name: String? = null,
    @SerialName("title") val title: String? = null,
    val description: String? = null,
    val ingredients: List<String> = emptyList(),
    val uses: List<String> = emptyList(),
    val extra: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    @SerialName("instructions") val instructions: List<String> = emptyList(),
    val minutes: Int? = null,
    @SerialName("cook_time_minutes") val cookTimeMinutes: Int? = null
)

@Serializable
data class RecommendedTabResponse(
    val recipes: List<LibraryRecipeOut> = emptyList()
)
