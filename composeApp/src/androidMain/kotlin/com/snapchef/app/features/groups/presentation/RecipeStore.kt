package com.snapchef.app.features.groups.presentation

import com.snapchef.app.core.auth.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject

object RecipeStore {
    private const val KEY_SAVED_RECIPES_PREFIX = "saved_recipes_user_"
    private const val KEY_FAVORITES_PREFIX = "favorite_keys_user_"
    private const val LEGACY_KEY_SAVED_RECIPES = "saved_recipes"
    private const val LEGACY_KEY_FAVORITES = "favorite_keys"
    private val _personalRecipes = MutableStateFlow<List<SharedRecipe>>(emptyList())
    val personalRecipes: StateFlow<List<SharedRecipe>> = _personalRecipes.asStateFlow()
    private val _sharedRecipesByGroup = MutableStateFlow<Map<String, List<SharedRecipe>>>(emptyMap())
    val sharedRecipesByGroup: StateFlow<Map<String, List<SharedRecipe>>> = _sharedRecipesByGroup.asStateFlow()
    private val _favoriteRecipeKeys = MutableStateFlow<Set<String>>(emptySet())
    val favoriteRecipeKeys: StateFlow<Set<String>> = _favoriteRecipeKeys.asStateFlow()

    init {
        reloadFromStorageForCurrentUser()
    }

    fun reloadFromStorageForCurrentUser() {
        migrateLegacyKeysIfNeeded()
        _personalRecipes.value = loadSavedRecipes()
        _favoriteRecipeKeys.value = loadFavoriteKeys()
    }

    fun addPersonalRecipe(recipe: SharedRecipe) {
        _personalRecipes.update { current ->
            val updated = (listOf(recipe) + current).distinctBy { it.favoriteKey() }
            persistSavedRecipes(updated)
            updated
        }
    }

    fun replacePersonalRecipes(recipes: List<SharedRecipe>) {
        val merged = (recipes + _personalRecipes.value).distinctBy { it.favoriteKey() }
        _personalRecipes.value = merged
        persistSavedRecipes(merged)
    }

    fun removePersonalRecipe(recipe: SharedRecipe) {
        _personalRecipes.update { current ->
            val updated = current.filterNot { it.favoriteKey() == recipe.favoriteKey() }
            persistSavedRecipes(updated)
            updated
        }
        removeFavoriteLocal(recipe)
    }

    fun addSharedRecipe(groupId: String, recipe: SharedRecipe) {
        _sharedRecipesByGroup.update { current ->
            val groupRecipes = current[groupId].orEmpty()
            current + (groupId to (listOf(recipe) + groupRecipes))
        }
    }

    fun setSharedRecipesForGroup(groupId: String, recipes: List<SharedRecipe>) {
        _sharedRecipesByGroup.update { it + (groupId to recipes) }
    }

    fun removeSharedRecipe(groupId: String, recipe: SharedRecipe) {
        _sharedRecipesByGroup.update { current ->
            val updatedList = current[groupId].orEmpty().filterNot { it.favoriteKey() == recipe.favoriteKey() }
            current + (groupId to updatedList)
        }
        removeFavoriteLocal(recipe)
    }

    fun sharedRecipesForGroup(groupId: String): List<SharedRecipe> {
        return _sharedRecipesByGroup.value[groupId].orEmpty()
    }

    fun toggleFavoriteLocal(recipe: SharedRecipe) {
        val key = recipe.favoriteKey()
        _favoriteRecipeKeys.update { current ->
            val updated = if (key in current) current - key else current + key
            persistFavoriteKeys(updated)
            updated
        }
    }

    fun removeFavoriteLocal(recipe: SharedRecipe) {
        val key = recipe.favoriteKey()
        _favoriteRecipeKeys.update { current ->
            val updated = current - key
            persistFavoriteKeys(updated)
            updated
        }
    }

    fun isFavoriteLocal(recipe: SharedRecipe): Boolean {
        return recipe.favoriteKey() in _favoriteRecipeKeys.value
    }

    fun setFavoriteKeys(keys: Set<String>) {
        _favoriteRecipeKeys.value = keys
        persistFavoriteKeys(keys)
    }

    private fun userScopedKey(prefix: String): String {
        val uid = AuthManager.currentUser?.id ?: 0
        return "$prefix$uid"
    }

    private fun persistFavoriteKeys(keys: Set<String>) {
        val arr = JSONArray()
        keys.forEach { arr.put(it) }
        AuthManager.putString(userScopedKey(KEY_FAVORITES_PREFIX), arr.toString())
    }

    private fun loadFavoriteKeys(): Set<String> {
        val raw = AuthManager.getString(userScopedKey(KEY_FAVORITES_PREFIX)) ?: return emptySet()
        return runCatching {
            val arr = JSONArray(raw)
            buildSet {
                for (i in 0 until arr.length()) add(arr.getString(i))
            }
        }.getOrDefault(emptySet())
    }

    private fun persistSavedRecipes(recipes: List<SharedRecipe>) {
        val arr = JSONArray()
        recipes.forEach { recipe ->
            val obj = JSONObject()
            obj.put("title", recipe.title)
            obj.put("description", recipe.description)
            obj.put("ownerName", recipe.ownerName)
            obj.put("sessionRecipeId", recipe.sessionRecipeId)
            obj.put("catalogRecipeId", recipe.catalogRecipeId)
            obj.put("backendSharedId", recipe.backendSharedId)
            obj.put("isCatalogStarred", recipe.isCatalogStarred)
            recipe.sharedByUserId?.let { obj.put("sharedByUserId", it) }
            obj.put("missingItems", JSONArray(recipe.missingItems))
            obj.put("availableItems", JSONArray(recipe.availableItems))
            obj.put("instructions", JSONArray(recipe.instructions))
            arr.put(obj)
        }
        AuthManager.putString(userScopedKey(KEY_SAVED_RECIPES_PREFIX), arr.toString())
    }

    private fun loadSavedRecipes(): List<SharedRecipe> {
        val raw = AuthManager.getString(userScopedKey(KEY_SAVED_RECIPES_PREFIX)) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        SharedRecipe(
                            title = obj.optString("title"),
                            description = obj.optString("description"),
                            ownerName = obj.optString("ownerName", "You"),
                            missingItems = obj.optJSONArray("missingItems").toStringList(),
                            availableItems = obj.optJSONArray("availableItems").toStringList(),
                            instructions = obj.optJSONArray("instructions").toStringList(),
                            backendSharedId = obj.optInt("backendSharedId").takeIf { it != 0 },
                            sessionRecipeId = obj.optInt("sessionRecipeId").takeIf { it != 0 },
                            catalogRecipeId = obj.optInt("catalogRecipeId").takeIf { it != 0 },
                            isCatalogStarred = if (obj.has("isCatalogStarred")) obj.optBoolean("isCatalogStarred") else null,
                            sharedByUserId = if (obj.has("sharedByUserId")) obj.optInt("sharedByUserId").takeIf { it != 0 } else null,
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun migrateLegacyKeysIfNeeded() {
        val scopedSavedKey = userScopedKey(KEY_SAVED_RECIPES_PREFIX)
        val scopedFavKey = userScopedKey(KEY_FAVORITES_PREFIX)
        val hasScopedSaved = AuthManager.getString(scopedSavedKey) != null
        val hasScopedFav = AuthManager.getString(scopedFavKey) != null
        val legacySaved = AuthManager.getString(LEGACY_KEY_SAVED_RECIPES)
        val legacyFav = AuthManager.getString(LEGACY_KEY_FAVORITES)

        if (!hasScopedSaved && !legacySaved.isNullOrBlank()) {
            AuthManager.putString(scopedSavedKey, legacySaved)
        }
        if (!hasScopedFav && !legacyFav.isNullOrBlank()) {
            AuthManager.putString(scopedFavKey, legacyFav)
        }
    }
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (i in 0 until length()) add(optString(i))
    }.filter { it.isNotBlank() }
}
