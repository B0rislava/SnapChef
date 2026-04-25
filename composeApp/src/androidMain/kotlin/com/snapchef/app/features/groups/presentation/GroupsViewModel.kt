package com.snapchef.app.features.groups.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapchef.app.core.auth.AuthManager
import com.snapchef.app.core.di.SnapChefServiceLocator
import com.snapchef.app.features.auth.data.remote.ShareRecipeRequest
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GroupsUiState(
    val groups: List<RecipeGroup> = emptyList(),
    val selectedGroupId: String = "personal",
    val expanded: Boolean = false,
    val dialogMode: GroupDialogMode? = null,
    val joinCodeInput: String = "",
    val createNameInput: String = "",
    val selectedRecipe: SharedRecipe? = null,
    val infoMessage: String? = null,
    val isError: Boolean = false,
    val isLoading: Boolean = false,
    val isDetailLoading: Boolean = false,
)

class GroupsViewModel : ViewModel() {
    private val apiService = SnapChefServiceLocator.authApiService
    private var lastSavedSyncAtMs: Long = 0L
    private var isSavedSyncRunning: Boolean = false
    private var lastDetailedGroupId: String? = null
    private var lastDetailedGroupLoadAtMs: Long = 0L

    private val _uiState = MutableStateFlow(defaultState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        refreshGroups()

        viewModelScope.launch {
            RecipeStore.personalRecipes.collect { savedRecipes ->
                _uiState.update { state ->
                    val updatedGroups = state.groups.map { group ->
                        if (group.id == "personal") {
                            group.copy(recipes = savedRecipes)
                        } else {
                            group
                        }
                    }
                    state.copy(groups = updatedGroups)
                }
            }
        }
    }

    fun refreshGroups() {
        viewModelScope.launch {
            if (!AuthManager.isLoggedIn()) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, infoMessage = null, isError = false) }
            try {
                val backendGroups = apiService.fetchGroups()
                _uiState.update { state ->
                    val mapped = backendGroups.map { g ->
                        val existing = state.groups.find { it.id == g.id.toString() }
                        RecipeGroup(
                            id = g.id.toString(),
                            name = g.name,
                            code = g.code ?: existing?.code, // Preserve old code if backend doesn't return it
                            recipes = existing?.recipes ?: emptyList(),
                            ownerUsername = if (g.createdByUserId == AuthManager.currentUser?.id) "You" else "Admin",
                            members = existing?.members ?: emptyList(),
                            isPersonal = false
                        )
                    }
                    val personalGroup = state.groups.first { it.isPersonal }
                    state.copy(
                        groups = listOf(personalGroup) + mapped.filter { m -> !m.id.startsWith("group_") },
                        isLoading = false
                    )
                }
                syncSavedRecipesFromBackend(force = false)
                refreshSelectedGroupDetail()
            } catch (e: Exception) {
                e.printStackTrace()
                val isUnauthorized = e is ClientRequestException && e.response.status.value == 401
                if (isUnauthorized) {
                    AuthManager.logout()
                }
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        infoMessage = if (isUnauthorized) "Session expired. Please log in again." else "Failed to load groups: ${e.message ?: "Unknown error"}",
                        isError = true
                    )
                }
            }
        }
    }

    private fun syncSavedRecipesFromBackend(force: Boolean) {
        viewModelScope.launch {
            if (!AuthManager.isLoggedIn()) return@launch
            val now = System.currentTimeMillis()
            if (!force && (isSavedSyncRunning || (now - lastSavedSyncAtMs) < 20_000L)) return@launch
            isSavedSyncRunning = true
            val catalog = runCatching { apiService.listCatalogFavoriteRecipes() }.getOrDefault(emptyList())
                .map { it.toUiSharedRecipe() }
            val session = runCatching { apiService.listFavoriteSessionRecipes() }.getOrDefault(emptyList())
                .map { it.toUiSharedRecipe() }
            val backendKnownRecipes = (session + catalog).distinctBy { it.favoriteKey() }
            // Import server-known saved recipes only when local is empty (upgrade/reinstall restore).
            if (RecipeStore.personalRecipes.value.isEmpty()) {
                RecipeStore.replacePersonalRecipes(backendKnownRecipes)
            }
            lastSavedSyncAtMs = now
            isSavedSyncRunning = false
        }
    }

    private fun refreshSelectedGroupDetail() {
        val selectedId = _uiState.value.selectedGroupId
        if (selectedId == "personal") {
            return
        }
        val idInt = selectedId.toIntOrNull() ?: return
        val now = System.currentTimeMillis()
        if (_uiState.value.isDetailLoading) return
        if (lastDetailedGroupId == selectedId && (now - lastDetailedGroupLoadAtMs) < 10_000L) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDetailLoading = true) }
            try {
                val detail = apiService.fetchGroupDetail(idInt)
                val sharedOut = runCatching {
                    apiService.listGroupSharedRecipes(idInt)
                }.getOrNull().orEmpty()
                val uid = AuthManager.currentUser?.id
                val sharedUi = sharedOut.map { it.toUiSharedRecipe(uid) }
                _uiState.update { state ->
                    val updatedGroups = state.groups.map { group ->
                        if (group.id == selectedId) {
                            group.copy(
                                code = detail.code,
                                members = detail.members.map { m ->
                                    GroupMember(
                                        username = if (m.user.id == AuthManager.currentUser?.id) "You" else m.user.name,
                                        avatarSeed = m.user.name,
                                        id = m.user.id
                                    )
                                },
                                recipes = sharedUi,
                            )
                        } else {
                            group
                        }
                    }
                    state.copy(groups = updatedGroups, isDetailLoading = false)
                }
                RecipeStore.setSharedRecipesForGroup(selectedId, sharedUi)
                lastDetailedGroupId = selectedId
                lastDetailedGroupLoadAtMs = now
            } catch (e: Exception) {
                _uiState.update { it.copy(isDetailLoading = false) }
                e.printStackTrace()
            }
        }
    }

    fun selectGroup(id: String) {
        _uiState.update { it.copy(selectedGroupId = id, expanded = false) }
        refreshSelectedGroupDetail()
    }
    fun openDialog(mode: GroupDialogMode) = _uiState.update { it.copy(dialogMode = mode) }
    fun closeDialog() = _uiState.update { it.copy(dialogMode = null) }
    fun setJoinCodeInput(value: String) = _uiState.update { it.copy(joinCodeInput = value) }
    fun setCreateNameInput(value: String) = _uiState.update { it.copy(createNameInput = value) }
    fun openRecipe(recipe: SharedRecipe) = _uiState.update { it.copy(selectedRecipe = recipe) }
    fun closeRecipeDetails() = _uiState.update { it.copy(selectedRecipe = null) }
    fun clearInfoMessage() {
        _uiState.update { it.copy(infoMessage = null, isError = false) }
    }

    fun shareRecipeToGroup(groupId: String, recipe: SharedRecipe) {
        val gid = groupId.toIntOrNull() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val ingredients = when {
                recipe.availableItems.isNotEmpty() || recipe.missingItems.isNotEmpty() ->
                    (recipe.availableItems + recipe.missingItems).distinct()
                else -> listOfNotNull(recipe.title.takeIf { it.isNotBlank() })
            }
            val result = runCatching {
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
            if (result.isSuccess) {
                val shared = runCatching { apiService.listGroupSharedRecipes(gid) }
                    .getOrNull()
                    .orEmpty()
                    .map { it.toUiSharedRecipe(AuthManager.currentUser?.id) }
                RecipeStore.setSharedRecipesForGroup(groupId, shared)
                _uiState.update { state ->
                    val updated = state.groups.map { g ->
                        if (g.id == groupId) g.copy(recipes = shared) else g
                    }
                    state.copy(
                        groups = updated,
                        selectedGroupId = groupId,
                        infoMessage = "Recipe shared to group.",
                        isError = false,
                        isLoading = false,
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        infoMessage = "Could not share recipe. Try again.",
                        isError = true,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun toggleRecipeFavorite(recipe: SharedRecipe) {
        // Favorites are intentionally local-only state, independent from "saved" cloud backup.
        RecipeStore.toggleFavoriteLocal(recipe)
    }

    fun deleteRecipe(recipe: SharedRecipe) {
        val selectedId = _uiState.value.selectedGroupId
        if (selectedId == "personal") {
            RecipeStore.removePersonalRecipe(recipe)
            return
        }

        if (!recipe.canCurrentUserDeleteFromGroup(AuthManager.currentUser?.id)) {
            _uiState.update {
                it.copy(
                    infoMessage = "Only the person who shared this can remove it.",
                    isError = true,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            recipe.backendSharedId?.let { sharedId ->
                runCatching { apiService.deleteSharedRecipe(sharedId) }
            }
            RecipeStore.removeSharedRecipe(selectedId, recipe)
            _uiState.update { state ->
                val updated = state.groups.map { group ->
                    if (group.id == selectedId) {
                        group.copy(recipes = group.recipes.filterNot { it.favoriteKey() == recipe.favoriteKey() })
                    } else group
                }
                state.copy(groups = updated, isLoading = false, infoMessage = "Recipe deleted.")
            }
        }
    }
    fun joinGroup() {
        val state = _uiState.value
        val code = state.joinCodeInput.trim()
        if (code.length < 4) {
            _uiState.update { it.copy(infoMessage = "Please enter a valid group code.", dialogMode = null, joinCodeInput = "") }
            return
        }
        val joinedAlready = state.groups.firstOrNull { it.code == code }
        if (joinedAlready != null) {
            _uiState.update {
                it.copy(
                    selectedGroupId = joinedAlready.id,
                    infoMessage = "You are already in this group.",
                    dialogMode = null,
                    joinCodeInput = "",
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val joined = apiService.joinGroup(code)
                _uiState.update {
                    it.copy(
                        selectedGroupId = joined.id.toString(),
                        infoMessage = "Joined group ${joined.name}.",
                        dialogMode = null,
                        joinCodeInput = "",
                        isLoading = false
                    )
                }
                refreshGroups()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        infoMessage = "Please enter a valid code.",
                        isError = true,
                        dialogMode = null,
                        joinCodeInput = "",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun createGroup() {
        val state = _uiState.value
        val groupName = state.createNameInput.trim()
        if (groupName.isBlank()) {
            _uiState.update { it.copy(infoMessage = "Group name cannot be empty.", dialogMode = null, createNameInput = "") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val created = apiService.createGroup(groupName)
                _uiState.update {
                    it.copy(
                        selectedGroupId = created.id.toString(),
                        infoMessage = "Group '${created.name}' created! Code: ${created.code ?: "N/A"}",
                        dialogMode = null,
                        createNameInput = "",
                        isLoading = false
                    )
                }
                refreshGroups()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("GroupsViewModel", "createGroup failed: ${e::class.simpleName}: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        infoMessage = "Failed to create group: ${e.message ?: e::class.simpleName}",
                        isError = true,
                        dialogMode = null,
                        createNameInput = "",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun renameSelectedGroup(newNameRaw: String) {
        val state = _uiState.value
        val selected = state.groups.firstOrNull { it.id == state.selectedGroupId } ?: return
        val newName = newNameRaw.trim()

        if (newName.isBlank()) {
            _uiState.update { it.copy(infoMessage = "Group name cannot be empty.") }
            return
        }

        if (!selected.ownerUsername.equals("You", ignoreCase = true)) {
            _uiState.update { it.copy(infoMessage = "Only the group admin can edit the group name.") }
            return
        }

        _uiState.update { current ->
            current.copy(
                groups = current.groups.map { group ->
                    if (group.id == selected.id) group.copy(name = newName) else group
                },
                infoMessage = "Group renamed to $newName.",
            )
        }
    }

    fun kickMemberFromSelectedGroup(member: GroupMember) {
        val state = _uiState.value
        val selected = state.groups.firstOrNull { it.id == state.selectedGroupId } ?: return
        val groupIdInt = selected.id.toIntOrNull() ?: return
        val memberId = member.id ?: return

        if (!selected.ownerUsername.equals("You", ignoreCase = true)) {
            _uiState.update { it.copy(infoMessage = "Only the group admin can remove members.") }
            return
        }

        if (member.username.equals("You", ignoreCase = true) ||
            member.username.equals(selected.ownerUsername, ignoreCase = true)
        ) {
            _uiState.update { it.copy(infoMessage = "Admin cannot be removed from the group.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                apiService.kickMember(groupIdInt, memberId)
                _uiState.update { current ->
                    current.copy(
                        infoMessage = "${member.username} was removed from ${selected.name}.",
                        isLoading = false
                    )
                }
                refreshSelectedGroupDetail()
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        infoMessage = "Failed to remove member. Error: ${e.message}",
                        isError = true
                    )
                }
            }
        }
    }


    fun leaveSelectedGroup() {
        val state = _uiState.value
        val selected = state.groups.firstOrNull { it.id == state.selectedGroupId } ?: return

        if (selected.isPersonal) {
            _uiState.update { it.copy(infoMessage = "Personal group cannot be left.") }
            return
        }

        if (selected.ownerUsername.equals("You", ignoreCase = true)) {
            _uiState.update { it.copy(infoMessage = "Admins cannot leave their own group. Delete it instead.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                apiService.leaveGroup(selected.id.toInt())
                _uiState.update {
                    it.copy(
                        selectedGroupId = "personal",
                        infoMessage = "You left ${selected.name}.",
                        isLoading = false
                    )
                }
                refreshGroups()
            } catch (e: Exception) {
                _uiState.update { it.copy(infoMessage = "Failed to leave group.", isLoading = false) }
            }
        }
    }

    fun deleteSelectedGroup() {
        val state = _uiState.value
        val selected = state.groups.firstOrNull { it.id == state.selectedGroupId } ?: return

        if (selected.isPersonal) {
            _uiState.update { it.copy(infoMessage = "Personal group cannot be deleted.") }
            return
        }

        if (!selected.ownerUsername.equals("You", ignoreCase = true)) {
            _uiState.update { it.copy(infoMessage = "Only the group admin can delete this group.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                apiService.deleteGroup(selected.id.toInt())
                _uiState.update {
                    it.copy(
                        selectedGroupId = "personal",
                        infoMessage = "Group ${selected.name} deleted.",
                        isLoading = false
                    )
                }
                refreshGroups()
            } catch (e: Exception) {
                _uiState.update { it.copy(infoMessage = "Failed to delete group.", isLoading = false) }
            }
        }
    }

    private fun defaultState(): GroupsUiState {
        val groups = listOf(
            RecipeGroup(
                id = "personal",
                name = "Your recipes",
                code = null,
                recipes = emptyList(),
                ownerUsername = "You",
                members = listOf(GroupMember("You")),
                isPersonal = true,
            )
        )
        return GroupsUiState(groups = groups, selectedGroupId = "personal")
    }
}
