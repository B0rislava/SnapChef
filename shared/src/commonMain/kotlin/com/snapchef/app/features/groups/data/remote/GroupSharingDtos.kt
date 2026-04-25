package com.snapchef.app.features.groups.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShareRecipeToGroupRequest(
    @SerialName("group_id") val groupId: Int,
    val title: String,
    val description: String? = null,
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    @SerialName("recipe_id") val recipeId: Int? = null,
    @SerialName("session_recipe_id") val sessionRecipeId: Int? = null
)

@Serializable
data class GroupSharedRecipeOut(
    val id: Int,
    val title: String? = null,
    val name: String? = null,
    val description: String? = null,
    @SerialName("body") val body: String? = null,
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    @SerialName("author_name") val authorName: String? = null,
    @SerialName("owner_name") val ownerName: String? = null,
    @SerialName("shared_by") val sharedBy: String? = null,
    @SerialName("sharer_name") val sharerName: String? = null
)

@Serializable
data class CombinedPantryItemOut(
    val name: String = "",
    @SerialName("item_name") val itemName: String? = null,
    @SerialName("group_id") val groupId: Int? = null,
    @SerialName("user_name") val userName: String? = null,
    @SerialName("from_user") val fromUser: String? = null,
    @SerialName("contributed_by") val contributedBy: String? = null,
    val qty: String? = null,
    val unit: String? = null
)

@Serializable
data class CombinedPantryResponse(
    val items: List<CombinedPantryItemOut> = emptyList()
)
