package com.snapchef.app.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapchef.app.core.di.SnapChefServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CapturedPhoto(val id: Int, val bytes: ByteArray)

data class HomeUiState(
    val capturedPhotos: List<CapturedPhoto> = emptyList(),
    val nextPhotoId: Int = 0,

    val showPhotoReview: Boolean = false,
    val showIngredientModal: Boolean = false,

    val isAnalyzing: Boolean = false,
    val ingredients: List<String> = emptyList(),
    
    val currentSessionId: Int? = null,
    val errorMessage: String? = null
) {
    val capturedCount: Int get() = capturedPhotos.size
}

class HomeViewModel : ViewModel() {
    private val apiService = SnapChefServiceLocator.homeApiService
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun capturePhoto(bytes: ByteArray) {
        _uiState.update {
            it.copy(
                capturedPhotos = it.capturedPhotos + CapturedPhoto(it.nextPhotoId, bytes),
                nextPhotoId = it.nextPhotoId + 1,
            )
        }
    }

    fun removePhoto(id: Int) {
        _uiState.update {
            it.copy(capturedPhotos = it.capturedPhotos.filter { ph -> ph.id != id })
        }
    }

    fun resetCameraCaptures() {
        _uiState.update { it.copy(capturedPhotos = emptyList(), nextPhotoId = 0) }
    }

    fun openPhotoReview() = _uiState.update { it.copy(showPhotoReview = true) }
    fun dismissPhotoReview() = _uiState.update { it.copy(showPhotoReview = false) }

    fun startAnalysis(images: List<ByteArray>) {
        _uiState.update {
            it.copy(
                showPhotoReview = false,
                showIngredientModal = true,
                isAnalyzing = true,
                ingredients = emptyList(),
                errorMessage = null,
                currentSessionId = null
            )
        }
        viewModelScope.launch {
            try {
                // 1. Upload scan
                val scanResponse = apiService.scanFridgeImages(images)
                
                if (scanResponse.items.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            errorMessage = "No products were recognized in the photos. Please try capturing a clearer picture."
                        )
                    }
                    return@launch
                }

                // 2. We got items, now confirm the session so we can generate recipes
                try {
                    apiService.confirmSession(scanResponse.id)
                } catch (e: Exception) {
                    // It might fail if the server logic or routes have issues, or if items are invalid.
                    // For the hackathon, we can log it, but we should proceed with the flow if we have items.
                    e.printStackTrace()
                }
                
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        ingredients = scanResponse.items.map { item -> item.name },
                        currentSessionId = scanResponse.id
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = "Failed to analyze ingredients: ${e.message}"
                    )
                }
            }
        }
    }

    fun dismissIngredientModal() = _uiState.update { it.copy(showIngredientModal = false, errorMessage = null) }

    // ── Ingredient editing ───────────────────────────────────────────────

    fun addIngredient(item: String) {
        if (item.isBlank()) return
        _uiState.update { it.copy(ingredients = it.ingredients + item.trim()) }
    }

    fun removeIngredient(item: String) {
        _uiState.update { it.copy(ingredients = it.ingredients.filter { i -> i != item }) }
    }
}

