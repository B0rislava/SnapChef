package com.snapchef.app.features.home.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class HomeUiState(
    val detectedIngredients: List<String> = emptyList(),
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun applyBackendJson(json: String) {
        runCatching {
            val obj = JSONObject(json)
            val ingredients = obj.optJSONArray("ingredients") ?: JSONArray()
            _uiState.value = HomeUiState(
                detectedIngredients = buildList {
                    for (i in 0 until ingredients.length()) add(ingredients.optString(i))
                },
            )
        }
    }
}

