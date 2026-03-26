package com.snapchef.app.features.profile.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class ProfileSavedRecipe(
    val title: String,
    val isQuick: Boolean,
)

data class ProfileUiState(
    val recipes: List<ProfileSavedRecipe> = listOf(
        ProfileSavedRecipe("Omelette with Cheese", true),
        ProfileSavedRecipe("Tomato Egg Fried Rice", true),
        ProfileSavedRecipe("Baked Veggie Pasta", false),
        ProfileSavedRecipe("Leftover Chicken Wraps", true),
    ),
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun applyBackendJson(json: String) {
        runCatching {
            val obj = JSONObject(json)
            val array = obj.optJSONArray("recipes") ?: JSONArray()
            val mapped = buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    add(
                        ProfileSavedRecipe(
                            title = item.optString("title", "Untitled recipe"),
                            isQuick = item.optBoolean("isQuick", false),
                        ),
                    )
                }
            }
            if (mapped.isNotEmpty()) _uiState.value = _uiState.value.copy(recipes = mapped)
        }
    }
}

