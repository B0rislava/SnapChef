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
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

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
                        if (group.id != "personal") {
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
                    state.copy(
                        groups = listOf(state.groups.first { it.isPersonal }) + mapped,
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
        val code = state.joinCodeInput.trim().uppercase()
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

    fun applyBackendJson(json: String) {
        runCatching {
            val root = JSONObject(json)
            val array = root.optJSONArray("groups") ?: JSONArray()
            val mappedGroups = buildList {
                add(defaultState().groups.first())
                for (i in 0 until array.length()) {
                    val g = array.optJSONObject(i) ?: continue
                    val recipesArray = g.optJSONArray("recipes") ?: JSONArray()
                    val recipes = buildList {
                        for (r in 0 until recipesArray.length()) {
                            val item = recipesArray.optJSONObject(r) ?: continue
                            add(
                                SharedRecipe(
                                    title = item.optString("title", "Untitled recipe"),
                                    description = item.optString("description", ""),
                                    ownerName = item.optString("ownerName", "Member"),
                                    missingItems = item.optJSONArray("missingItems")
                                        ?.let { miss ->
                                            buildList {
                                                for (m in 0 until miss.length()) add(miss.optString(m))
                                            }
                                        }.orEmpty(),
                                    availableItems = item.optJSONArray("availableItems")
                                        ?.let { list ->
                                            buildList {
                                                for (idx in 0 until list.length()) add(list.optString(idx))
                                            }
                                        }.orEmpty(),
                                    instructions = item.optJSONArray("instructions")
                                        ?.let { steps ->
                                            buildList {
                                                for (s in 0 until steps.length()) add(steps.optString(s))
                                            }
                                        }.orEmpty(),
                                    perishableProducts = item.optJSONArray("perishableProducts")
                                        ?.let { products ->
                                            buildList {
                                                for (p in 0 until products.length()) {
                                                    val product = products.optJSONObject(p) ?: continue
                                                    add(
                                                        PerishableProduct(
                                                            name = product.optString("name", "Perishable item"),
                                                            maxFreshDays = product.optInt("maxFreshDays", 3),
                                                            freshness = product.optDouble("freshness", 1.0).toFloat(),
                                                        ),
                                                    )
                                                }
                                            }
                                        }.orEmpty(),
                                ),
                            )
                        }
                    }
                    add(
                        RecipeGroup(
                            id = g.optString("id", "g_${i + 1}"),
                            name = g.optString("name", "Group ${i + 1}"),
                            code = if (g.has("code") && !g.isNull("code")) g.optString("code") else null,
                            recipes = recipes,
                            ownerUsername = g.optString("ownerUsername").takeIf { it.isNotBlank() },
                            members = g.optJSONArray("members")?.let { membersArray ->
                                buildList {
                                    for (m in 0 until membersArray.length()) {
                                        val item = membersArray.optJSONObject(m) ?: continue
                                        val username = item.optString("username", "").trim()
                                        if (username.isNotEmpty()) {
                                            add(
                                                GroupMember(
                                                    username = username,
                                                    avatarSeed = item.optString("avatarSeed", username),
                                                )
                                            )
                                        }
                                    }
                                }
                            }.orEmpty(),
                        ),
                    )
                }
            }
            _uiState.update { it.copy(groups = mappedGroups) }
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

private fun generateGroupCode(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return buildString {
        repeat(6) { append(chars[Random.nextInt(chars.length)]) }
    }
}
