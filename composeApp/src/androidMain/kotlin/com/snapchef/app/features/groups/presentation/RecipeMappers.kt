package com.snapchef.app.features.groups.presentation

import com.snapchef.app.features.auth.data.remote.RecipeOut
import com.snapchef.app.features.auth.data.remote.SharedRecipeOut
import com.snapchef.app.features.home.data.remote.SessionRecipeOut

fun SharedRecipeOut.toUiSharedRecipe(currentUserId: Int?): SharedRecipe {
    val owner = when {
        currentUserId != null && userId == currentUserId -> "You"
        else -> "Member"
    }
    return SharedRecipe(
        title = title,
        description = description.orEmpty(),
        ownerName = owner,
        missingItems = emptyList(),
        availableItems = ingredients,
        instructions = steps,
        backendSharedId = id,
        sharedByUserId = userId,
    )
}

fun RecipeOut.toUiSharedRecipe(): SharedRecipe {
    return SharedRecipe(
        title = title,
        description = description.orEmpty(),
        ownerName = "You",
        missingItems = emptyList(),
        availableItems = ingredients,
        instructions = steps,
        catalogRecipeId = id,
        isCatalogStarred = starred,
    )
}

fun SessionRecipeOut.toUiSharedRecipe(): SharedRecipe {
    return SharedRecipe(
        title = name,
        description = "Ready in ${minutes ?: "?"} mins",
        ownerName = "You",
        missingItems = extra,
        availableItems = uses,
        instructions = steps,
        sessionRecipeId = id,
        isCatalogStarred = favorited,
    )
}

/** Stable id for local-only fallbacks; prefer [SharedRecipe.favoriteKey]. */
fun SharedRecipe.favoriteKey(): String {
    catalogRecipeId?.let { return "cat:$it" }
    sessionRecipeId?.let { return "sess:$it" }
    backendSharedId?.let { return "share:$it" }
    return "local:${title.lowercase()}::${instructions.joinToString("|").lowercase()}"
}
