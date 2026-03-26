package com.snapchef.app.features.auth.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

data class WelcomeUiState(
    val title: String = "SnapChef",
    val subtitle: String = "Snap a photo, discover a recipe.\nCook smarter every day.",
)

class WelcomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()

    fun applyBackendJson(json: String) {
        runCatching {
            val obj = JSONObject(json)
            _uiState.value = _uiState.value.copy(
                title = obj.optString("title", _uiState.value.title),
                subtitle = obj.optString("subtitle", _uiState.value.subtitle),
            )
        }
    }
}

