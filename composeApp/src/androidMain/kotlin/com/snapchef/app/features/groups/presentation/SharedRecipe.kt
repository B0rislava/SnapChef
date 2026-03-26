package com.snapchef.app.features.groups.presentation

data class SharedRecipe(
    val title: String,
    val description: String,
    val ownerName: String,
    val missingItems: List<String>,
)

