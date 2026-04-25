package com.snapchef.app.features.home.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapchef.app.core.auth.AuthManager
import com.snapchef.app.core.di.SnapChefServiceLocator
import com.snapchef.app.core.presentation.components.MainTab
import com.snapchef.app.features.auth.data.remote.ShareRecipeRequest
import com.snapchef.app.features.groups.presentation.RecipeStore
import com.snapchef.app.features.groups.presentation.SharedRecipe
import com.snapchef.app.features.groups.presentation.toUiSharedRecipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileInventoryItem(
    val pantryItemIds: List<Int> = emptyList(),
    val name: String,
    val category: String,
    val quantity: String,
)

data class ActiveRecipeSessionSession(val sessionId: Int, val ingredients: List<String>)

data class MainUiState(
    val currentTab: MainTab = MainTab.HOME,
    val userName: String = "",
    val userEmail: String = "",
    val profileImageUri: Uri? = null,
    val isEditingProfile: Boolean = false,
    val activeRecipeSession: ActiveRecipeSessionSession? = null,
    val isCameraActive: Boolean = false,
    val inventoryItems: List<ProfileInventoryItem> = emptyList(),
)

class MainViewModel : ViewModel() {
    private val apiService = SnapChefServiceLocator.authApiService
    private var isCloudBackupSyncRunning: Boolean = false

    private val _uiState = MutableStateFlow(
        MainUiState(
            userName = AuthManager.currentUser?.name ?: "SnapChef User",
            userEmail = AuthManager.currentUser?.email ?: "user@snapchef.app",
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // Ensure saved/favorite recipes are always loaded for the current user on app start.
        RecipeStore.reloadFromStorageForCurrentUser()
        syncSavedRecipesToCloudBackup()
    }

    fun selectTab(tab: MainTab) {
        _uiState.update {
            it.copy(
                currentTab = tab,
                isEditingProfile = if (tab == MainTab.PROFILE) it.isEditingProfile else false,
            )
        }
        if (tab == MainTab.PROFILE) {
            refreshPantryItems()
        }
    }

    private fun refreshPantryItems() {
        viewModelScope.launch {
            try {
                val items = apiService.fetchPantryItems()
                _uiState.update { state ->
                    // Group by normalized name (lowercase) to unify "Egg" and "egg"
                    val unifiedItems = items.groupBy { it.name.trim().lowercase() }
                        .map { (_, group) ->
                            val firstName = group.first().name.trim().replaceFirstChar { it.uppercase() }
                            val totalQty = group.sumOf { it.quantity }
                            // If they have different units, we'll use the unit of the first item for simplicity, 
                            // or just the quantity if units are missing.
                            val firstUnit = group.firstOrNull { it.unit != null }?.unit
                            val quantityStr = if (firstUnit != null) "$totalQty $firstUnit" else "$totalQty"
                            
                            ProfileInventoryItem(
                                pantryItemIds = group.map { it.id },
                                name = firstName,
                                category = if (group.any { it.source == "scan" }) "Scanned" else "Manual",
                                quantity = quantityStr
                            )
                        }

                    state.copy(inventoryItems = unifiedItems)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startEditProfile() = _uiState.update { it.copy(isEditingProfile = true) }
    fun cancelEditProfile() = _uiState.update { it.copy(isEditingProfile = false) }

    fun saveProfile(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        currentPassword: String,
    ) {
        viewModelScope.launch {
            runCatching { apiService.updateProfile(name = name) }
            if (password.isNotBlank()) {
                runCatching {
                    apiService.changePassword(
                        currentPassword = currentPassword,
                        newPassword = password,
                    )
                }
            }
            val me = runCatching { apiService.getCurrentUser() }.getOrNull()
            val resolvedName = me?.name ?: name
            val resolvedEmail = me?.email ?: email

            AuthManager.currentUser?.let { current ->
                AuthManager.currentUser = current.copy(name = resolvedName, email = resolvedEmail)
            }

            _uiState.update {
                it.copy(userName = resolvedName, userEmail = resolvedEmail, isEditingProfile = false)
            }
        }
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

    fun markInventoryItemEaten(item: ProfileInventoryItem) {
        viewModelScope.launch {
            item.pantryItemIds.forEach { pantryItemId ->
                runCatching { apiService.deletePantryItem(pantryItemId) }
            }
            refreshPantryItems()
        }
    }

    fun openRecipeResults(sessionId: Int, ingredients: List<String>) {
        _uiState.update { it.copy(activeRecipeSession = ActiveRecipeSessionSession(sessionId, ingredients)) }
    }

    fun closeRecipeResults() {
        _uiState.update { it.copy(activeRecipeSession = null) }
    }

    fun setCameraActive(active: Boolean) {
        _uiState.update { it.copy(isCameraActive = active) }
    }
    
    fun saveGeneratedRecipe(recipe: SharedRecipe, isShared: Boolean, targetGroupId: String? = null) {
        viewModelScope.launch {
            if (isShared) {
                val gid = targetGroupId?.toIntOrNull() ?: return@launch
                val ingredients = when {
                    recipe.availableItems.isNotEmpty() || recipe.missingItems.isNotEmpty() ->
                        (recipe.availableItems + recipe.missingItems).distinct()
                    else -> listOfNotNull(recipe.title.takeIf { it.isNotBlank() })
                }
                runCatching {
                    apiService.shareRecipe(
                        ShareRecipeRequest(
                            groupId = gid,
                            title = recipe.title,
                            description = recipe.description.ifBlank { null },
                            ingredients = ingredients,
                            steps = recipe.instructions,
                            minutes = null,
                            note = null,
                            sessionRecipeId = recipe.sessionRecipeId,
                            recipeId = recipe.catalogRecipeId,
                        )
                    )
                }
                val shared = runCatching { apiService.listGroupSharedRecipes(gid) }
                    .getOrNull()
                    .orEmpty()
                    .map { it.toUiSharedRecipe(AuthManager.currentUser?.id) }
                RecipeStore.setSharedRecipesForGroup(gid.toString(), shared)
            } else {
                // Save must not implicitly favorite. It only adds to saved recipes.
                RecipeStore.addPersonalRecipe(recipe.copy(isCatalogStarred = false))
                syncSavedRecipesToCloudBackup()
            }
            _uiState.update {
                it.copy(
                    activeRecipeSession = null,
                    currentTab = MainTab.RECIPES,
                )
            }
        }
    }

    private fun syncSavedRecipesToCloudBackup() {
        if (isCloudBackupSyncRunning || !AuthManager.isLoggedIn()) return
        viewModelScope.launch {
            isCloudBackupSyncRunning = true
            val saved = RecipeStore.personalRecipes.value
            if (saved.isEmpty()) {
                isCloudBackupSyncRunning = false
                return@launch
            }

            val cloudCatalogIds = runCatching { apiService.listCatalogFavoriteRecipes() }
                .getOrDefault(emptyList())
                .map { it.id }
                .toMutableSet()
            val cloudSessionIds = runCatching { apiService.listFavoriteSessionRecipes() }
                .getOrDefault(emptyList())
                .map { it.id }
                .toMutableSet()

            saved.forEach { recipe ->
                recipe.catalogRecipeId?.let { id ->
                    if (id !in cloudCatalogIds) {
                        runCatching { apiService.starCatalogRecipe(id) }
                        cloudCatalogIds.add(id)
                    }
                }
                recipe.sessionRecipeId?.let { id ->
                    if (id !in cloudSessionIds) {
                        runCatching { apiService.favoriteSessionRecipe(id) }
                        cloudSessionIds.add(id)
                    }
                }
            }
            isCloudBackupSyncRunning = false
        }
    }

    fun logout() {
        AuthManager.logout()
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
                    activeRecipeSession = null,
                )
            }
        }
    }

}

