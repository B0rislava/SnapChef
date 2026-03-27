package com.snapchef.app.features.home.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapchef.app.core.auth.AuthManager
import com.snapchef.app.core.data.remote.createHttpClient
import com.snapchef.app.core.presentation.components.MainTab
import com.snapchef.app.features.auth.data.remote.AuthApiService
import com.snapchef.app.features.groups.presentation.RecipeStore
import com.snapchef.app.features.groups.presentation.SharedRecipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileInventoryItem(
    val name: String,
    val category: String,
    val quantity: String,
)

data class MainUiState(
    val currentTab: MainTab = MainTab.HOME,
    val userName: String = "",
    val userEmail: String = "",
    val profileImageUri: Uri? = null,
    val isEditingProfile: Boolean = false,
    val activeRecipeIngredients: List<String>? = null,
    val shouldNavigateToAuth: Boolean = false,
    val isCameraActive: Boolean = false,
    val inventoryItems: List<ProfileInventoryItem> = defaultInventoryItems(),
)

class MainViewModel : ViewModel() {
    private val apiService = AuthApiService(createHttpClient())

    private val _uiState = MutableStateFlow(
        MainUiState(
            userName = AuthManager.currentUser?.name ?: "SnapChef User",
            userEmail = AuthManager.currentUser?.email ?: "user@snapchef.app",
        )
    )
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

    fun addInventoryItem() {
        _uiState.update {
            it.copy(
                inventoryItems = it.inventoryItems + ProfileInventoryItem(
                    name = "",
                    category = "Pantry",
                    quantity = "",
                )
            )
        }
    }

    fun updateInventoryItem(index: Int, item: ProfileInventoryItem) {
        _uiState.update { state ->
            if (index !in state.inventoryItems.indices) return@update state
            val updated = state.inventoryItems.toMutableList().apply { this[index] = item }
            state.copy(inventoryItems = updated)
        }
    }

    fun removeInventoryItem(index: Int) {
        _uiState.update { state ->
            if (index !in state.inventoryItems.indices) return@update state
            state.copy(inventoryItems = state.inventoryItems.filterIndexed { i, _ -> i != index })
        }
    }

    fun openRecipeResults(ingredients: List<String>) {
        _uiState.update { it.copy(activeRecipeIngredients = ingredients) }
    }

    fun closeRecipeResults() {
        _uiState.update { it.copy(activeRecipeIngredients = null) }
    }

    fun setCameraActive(active: Boolean) {
        _uiState.update { it.copy(isCameraActive = active) }
    }
    
    fun saveGeneratedRecipe(recipe: SharedRecipe, isShared: Boolean) {
        if (isShared) {
            RecipeStore.addSharedRecipe(recipe)
        } else {
            RecipeStore.addPersonalRecipe(recipe)
        }
        _uiState.update {
            it.copy(
                activeRecipeIngredients = null,
                currentTab = MainTab.RECIPES,
            )
        }
    }

    fun logout() {
        AuthManager.logout()
        _uiState.update { it.copy(shouldNavigateToAuth = true) }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                apiService.deleteAccount()
            } catch (e: Exception) {
                // If it fails on backend (e.g. network error), we still want to log them out locally
                e.printStackTrace()
            }
            AuthManager.logout()
            _uiState.update {
                it.copy(
                    userName = "",
                    userEmail = "",
                    profileImageUri = null,
                    isEditingProfile = false,
                    activeRecipeIngredients = null,
                    shouldNavigateToAuth = true,
                )
            }
        }
    }

    fun onAuthNavigationHandled() {
        _uiState.update { it.copy(shouldNavigateToAuth = false) }
    }

}

private fun defaultInventoryItems(): List<ProfileInventoryItem> {
    return listOf(
        ProfileInventoryItem("Eggs", "Protein", "6"),
        ProfileInventoryItem("Cheddar Cheese", "Dairy", "200 g"),
        ProfileInventoryItem("Tomatoes", "Produce", "3"),
        ProfileInventoryItem("Chicken Breast", "Protein", "400 g"),
        ProfileInventoryItem("Pasta", "Pantry", "500 g"),
        ProfileInventoryItem("Spinach", "Produce", "100 g"),
        ProfileInventoryItem("Milk", "Dairy", "1 L"),
        ProfileInventoryItem("Olive Oil", "Pantry", "1 bottle"),
    )
}
