package com.snapchef.app.features.groups.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapchef.app.core.auth.AuthManager
import com.snapchef.app.core.di.SnapChefServiceLocator
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
)

class GroupsViewModel : ViewModel() {
    private val apiService = SnapChefServiceLocator.authApiService

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
        viewModelScope.launch {
            RecipeStore.sharedRecipes.collect { shared ->
                _uiState.update { state ->
                    val updatedGroups = state.groups.map { group ->
                        if (group.id != "personal" && !group.id.startsWith("group_")) {
                            group.copy(recipes = shared)
                        } else {
                            group
                        }
                    }
                    state.copy(groups = updatedGroups)
                }
            }
        }
    }

    private fun refreshGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
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
                    val mockGroups = state.groups.filter { it.id.startsWith("group_") }
                    val personalGroup = state.groups.first { it.isPersonal }
                    state.copy(
                        groups = (listOf(personalGroup) + mockGroups) + mapped.filter { m -> !m.id.startsWith("group_") },
                        isLoading = false
                    )
                }
                refreshSelectedGroupDetail()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                e.printStackTrace()
            }
        }
    }

    private fun refreshSelectedGroupDetail() {
        val selectedId = _uiState.value.selectedGroupId
        if (selectedId == "personal") return
        val idInt = selectedId.toIntOrNull() ?: return

        viewModelScope.launch {
            try {
                val detail = apiService.fetchGroupDetail(idInt)
                _uiState.update { state ->
                    val updatedGroups = state.groups.map { group ->
                        if (group.id == selectedId) {
                            group.copy(
                                code = detail.code,
                                members = detail.members.map { m ->
                                    GroupMember(
                                        username = if (m.user.id == AuthManager.currentUser?.id) "You" else m.user.name,
                                        avatarSeed = m.user.name
                                    )
                                }
                            )
                        } else {
                            group
                        }
                    }
                    state.copy(groups = updatedGroups)
                }
            } catch (e: Exception) {
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
                _uiState.update { 
                    it.copy(
                        infoMessage = "Failed to create group. Try again.",
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

    fun kickMemberFromSelectedGroup(username: String) {
        val state = _uiState.value
        val selected = state.groups.firstOrNull { it.id == state.selectedGroupId } ?: return

        if (!selected.ownerUsername.equals("You", ignoreCase = true)) {
            _uiState.update { it.copy(infoMessage = "Only the group admin can remove members.") }
            return
        }

        if (username.equals("You", ignoreCase = true) ||
            username.equals(selected.ownerUsername, ignoreCase = true)
        ) {
            _uiState.update { it.copy(infoMessage = "Admin cannot be removed from the group.") }
            return
        }

        val updatedMembers = selected.members.filterNot { it.username.equals(username, ignoreCase = true) }
        if (updatedMembers.size == selected.members.size) {
            _uiState.update { it.copy(infoMessage = "Member not found in this group.") }
            return
        }

        _uiState.update { current ->
            current.copy(
                groups = current.groups.map { group ->
                    if (group.id == selected.id) group.copy(members = updatedMembers) else group
                },
                infoMessage = "$username was removed from ${selected.name}.",
            )
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
            ),
            RecipeGroup(
                id = "group_1",
                name = "Family & Friends",
                code = "FOOD12",
                ownerUsername = "You",
                members = listOf(
                    GroupMember("You"),
                    GroupMember("Elena"),
                    GroupMember("Ivan"),
                ),
                isPersonal = false,
                recipes = listOf(
                    SharedRecipe(
                        title = "Morning Cloud Eggs",
                        description = "A light and fluffy breakfast suggested by AI based on your group's shared ingredients!",
                        ownerName = "AI Suggestion",
                        availableItems = listOf("Eggs (from You)", "Spinach (from Elena)", "Whole Wheat Bread (from Ivan)"),
                        missingItems = listOf("Black Pepper", "Olive Oil"),
                        instructions = listOf(
                            "Separate the egg whites from yolks.",
                            "Whip the whites with a pinch of salt until stiff peaks form.",
                            "Gently fold in the chopped spinach provided by Elena.",
                            "Spoon the whites onto the bread provided by Ivan and create a small well in the center.",
                            "Bake at 200°C for 3 minutes, then add the yolk to the well and bake for another 3 minutes.",
                            "Garnish with black pepper if available."
                        ),
                        perishableProducts = listOf(
                            PerishableProduct("Eggs", 7, 0.8f),
                            PerishableProduct("Spinach", 3, 0.4f),
                            PerishableProduct("Bread", 5, 0.6f)
                        )
                    )
                )
            ),
            RecipeGroup(
                id = "group_2",
                name = "Chef Squad",
                code = "CHEF01",
                ownerUsername = "Elena",
                members = listOf(
                    GroupMember("You"),
                    GroupMember("Elena"),
                    GroupMember("Ivan"),
                    GroupMember("Sofia"),
                ),
                isPersonal = false,
                recipes = listOf(
                    SharedRecipe(
                        title = "Collaborative Veggie Stir-fry",
                        description = "A perfectly balanced stir-fry made entirely from ingredients your group already has! No shopping needed.",
                        ownerName = "AI Suggestion",
                        availableItems = listOf("Broccoli (from You)", "Carrots (from Elena)", "Tofu (from Ivan)", "Soy Sauce (from Sofia)"),
                        missingItems = emptyList(),
                        instructions = listOf(
                            "Press and cube the tofu provided by Ivan, then sauté until golden.",
                            "Add the sliced carrots from Elena and broccoli from You to the pan.",
                            "Stir-fry on high heat for 5-7 minutes until vegetables are tender-crisp.",
                            "Pour in the soy sauce from Sofia and toss well to coat everything.",
                            "Serve hot and enjoy the result of your team effort!"
                        ),
                        perishableProducts = listOf(
                            PerishableProduct("Broccoli", 4, 0.7f),
                            PerishableProduct("Carrots", 10, 0.9f),
                            PerishableProduct("Tofu", 6, 0.5f)
                        )
                    )
                )
            )
        )
        return GroupsUiState(groups = groups, selectedGroupId = "personal")
    }
}
