package com.snapchef.app.features.groups.presentation

data class GroupMember(
    val username: String,
    val avatarSeed: String = username,
    val id: Int? = null,
)

data class RecipeGroup(
    val id: String,
    val name: String,
    val code: String?,
    val recipes: List<SharedRecipe>,
    val ownerUsername: String? = null,
    val members: List<GroupMember> = emptyList(),
    val isPersonal: Boolean = false,
)

