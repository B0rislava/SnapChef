package com.snapchef.app.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class HomeUiState(

    val capturedPhotoIds: List<Int> = emptyList(),
    val nextPhotoId: Int = 0,

    val showPhotoReview: Boolean = false,
    val showIngredientModal: Boolean = false,

    val isAnalyzing: Boolean = false,
    val ingredients: List<String> = emptyList(),
) {
    val capturedCount: Int get() = capturedPhotoIds.size
}

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun capturePhoto() {
        _uiState.update {
            it.copy(
                capturedPhotoIds = it.capturedPhotoIds + it.nextPhotoId,
                nextPhotoId = it.nextPhotoId + 1,
            )
        }
    }

    fun removePhoto(id: Int) {
        _uiState.update {
            it.copy(capturedPhotoIds = it.capturedPhotoIds.filter { ph -> ph != id })
        }
    }


    fun resetCameraCaptures() {
        _uiState.update { it.copy(capturedPhotoIds = emptyList(), nextPhotoId = 0) }
    }


    fun openPhotoReview() = _uiState.update { it.copy(showPhotoReview = true) }
    fun dismissPhotoReview() = _uiState.update { it.copy(showPhotoReview = false) }

    fun startAnalysis(photoCount: Int) {
        _uiState.update {
            it.copy(
                showPhotoReview = false,
                showIngredientModal = true,
                isAnalyzing = true,
                ingredients = emptyList(),
            )
        }
        viewModelScope.launch {
            delay(1_500)
            val pool = listOf(
                "Tomatoes", "Eggs", "Cheese", "Onion",
                "Garlic", "Flour", "Butter", "Milk",
            )
            _uiState.update {
                it.copy(
                    isAnalyzing = false,
                    ingredients = pool.take(minOf(photoCount + 2, pool.size)),
                )
            }
        }
    }

    fun dismissIngredientModal() = _uiState.update { it.copy(showIngredientModal = false) }

    // ── Ingredient editing ───────────────────────────────────────────────

    fun addIngredient(item: String) {
        if (item.isBlank()) return
        _uiState.update { it.copy(ingredients = it.ingredients + item.trim()) }
    }

    fun removeIngredient(item: String) {
        _uiState.update { it.copy(ingredients = it.ingredients.filter { i -> i != item }) }
    }


    fun applyBackendJson(json: String) {
        runCatching {
            val obj = JSONObject(json)
            val arr = obj.optJSONArray("ingredients") ?: JSONArray()
            _uiState.update {
                it.copy(
                    isAnalyzing = false,
                    ingredients = buildList { for (i in 0 until arr.length()) add(arr.optString(i)) },
                )
            }
        }
    }
}

