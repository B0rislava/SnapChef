package com.snapchef.app.features.groups.presentation

import kotlin.math.ceil

data class PerishableProduct(
    val name: String,
    val maxFreshDays: Int,
    val freshness: Float,
) {
    fun daysLeft(): Int {
        // Maps slider 0..1 to [-1..maxFreshDays], where -1 means expired.
        return ceil((maxFreshDays + 1) * freshness).toInt() - 1
    }
}

data class SharedRecipe(
    val title: String,
    val description: String,
    val ownerName: String,
    val missingItems: List<String>,
    val availableItems: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val perishableProducts: List<PerishableProduct> = emptyList(),
    /** Shared recipe post id from `GET /share/group/{id}/recipes` */
    val backendSharedId: Int? = null,
    /** AI session recipe id (for `POST /ai/recipes/{id}/favorite`) */
    val sessionRecipeId: Int? = null,
    /** Starred catalog recipe from `GET /recipes/favorites` / recommended */
    val catalogRecipeId: Int? = null,
    /** When set, heart uses `GET /recipes/favorites` star state */
    val isCatalogStarred: Boolean? = null,
)

fun SharedRecipe.earliestDaysLeft(): Int? = perishableProducts.minOfOrNull { it.daysLeft() }
fun SharedRecipe.isExpired(): Boolean = (earliestDaysLeft() ?: Int.MAX_VALUE) < 0
fun SharedRecipe.expiresToday(): Boolean = (earliestDaysLeft() ?: Int.MAX_VALUE) == 0
fun SharedRecipe.spoiledProducts(): List<String> = perishableProducts.filter { it.daysLeft() < 0 }.map { it.name }

