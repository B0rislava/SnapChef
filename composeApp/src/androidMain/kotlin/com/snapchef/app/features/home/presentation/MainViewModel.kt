package com.snapchef.app.features.home.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.snapchef.app.core.presentation.components.MainTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject

data class MainUiState(
    val currentTab: MainTab = MainTab.HOME,
    val userName: String = "John Doe",
    val userEmail: String = "john.doe@snapchef.app",
    val profileImageUri: Uri? = null,
    val isEditingProfile: Boolean = false,
    val activeRecipeIngredients: List<String>? = null,
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun selectTab(tab: MainTab) {
        _uiState.update {
            it.copy(
                currentTab = tab,
                isEditingProfile = if (tab == MainTab.PROFILE) it.isEditingProfile else false,
            )
        }
    }

    fun startEditProfile() = _uiState.update { it.copy(isEditingProfile = true) }
    fun cancelEditProfile() = _uiState.update { it.copy(isEditingProfile = false) }

    fun saveProfile(name: String, email: String) {
        _uiState.update { it.copy(userName = name, userEmail = email, isEditingProfile = false) }
    }

    fun setProfileImage(uri: Uri) = _uiState.update { it.copy(profileImageUri = uri) }

    fun openRecipeResults(ingredients: List<String>) {
        _uiState.update { it.copy(activeRecipeIngredients = ingredients) }
    }

    fun closeRecipeResults() {
        _uiState.update { it.copy(activeRecipeIngredients = null) }
    }

    fun applyBackendJson(json: String) {
        runCatching {
            val obj = JSONObject(json)
            _uiState.update {
                it.copy(
                    userName = obj.optString("name", it.userName),
                    userEmail = obj.optString("email", it.userEmail),
                )
            }
        }
    }
}

