package com.snapchef.app.features.home.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class RecipeResultsUiState(
    val ingredients: List<String> = emptyList(),
    val recipes: List<String> = emptyList(),
)

class RecipeResultsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RecipeResultsUiState())
    val uiState: StateFlow<RecipeResultsUiState> = _uiState.asStateFlow()

    fun setIngredients(value: List<String>) {
        _uiState.value = _uiState.value.copy(ingredients = value)
    }

    fun applyBackendJson(json: String) {
        runCatching {
            val obj = JSONObject(json)
            val recipesArray = obj.optJSONArray("recipes") ?: JSONArray()
            val recipes = buildList {
                for (i in 0 until recipesArray.length()) {
                    val item = recipesArray.optJSONObject(i)
                    add(item?.optString("title") ?: recipesArray.optString(i))
                }
            }
            _uiState.value = _uiState.value.copy(recipes = recipes)
        }
    }
}

