package com.snapchef.app.features.groups.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
)

class GroupsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(defaultState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    fun setExpanded(value: Boolean) = _uiState.update { it.copy(expanded = value) }
    fun selectGroup(id: String) = _uiState.update { it.copy(selectedGroupId = id, expanded = false) }
    fun openDialog(mode: GroupDialogMode) = _uiState.update { it.copy(dialogMode = mode) }
    fun closeDialog() = _uiState.update { it.copy(dialogMode = null) }
    fun setJoinCodeInput(value: String) = _uiState.update { it.copy(joinCodeInput = value) }
    fun setCreateNameInput(value: String) = _uiState.update { it.copy(createNameInput = value) }
    fun openRecipe(recipe: SharedRecipe) = _uiState.update { it.copy(selectedRecipe = recipe) }
    fun closeRecipeDetails() = _uiState.update { it.copy(selectedRecipe = null) }

    fun joinGroup() {
        val state = _uiState.value
        val code = state.joinCodeInput.trim().uppercase()
        if (code.length < 4) {
            _uiState.update { it.copy(infoMessage = "Please enter a valid group code.", dialogMode = null, joinCodeInput = "") }
            return
        }
        val existing = state.groups.firstOrNull { it.code == code }
        if (existing != null) {
            _uiState.update {
                it.copy(
                    selectedGroupId = existing.id,
                    infoMessage = "You are already in this group.",
                    dialogMode = null,
                    joinCodeInput = "",
                )
            }
            return
        }
        val joined = RecipeGroup(
            id = "joined_${Random.nextInt(1000, 9999)}",
            name = "Group ${code.take(4)}",
            code = code,
            recipes = listOf(
                SharedRecipe(
                    title = "Shared Soup",
                    description = "Group shared soup recipe.",
                    ownerName = "Anton",
                    missingItems = listOf("2 eggs"),
                ),
            ),
        )
        _uiState.update {
            it.copy(
                groups = it.groups + joined,
                selectedGroupId = joined.id,
                infoMessage = "Joined group ${joined.name}.",
                dialogMode = null,
                joinCodeInput = "",
            )
        }
    }

    fun createGroup() {
        val state = _uiState.value
        val groupName = state.createNameInput.trim()
        if (groupName.isBlank()) {
            _uiState.update { it.copy(infoMessage = "Group name cannot be empty.", dialogMode = null, createNameInput = "") }
            return
        }
        val code = generateGroupCode()
        val created = RecipeGroup(
            id = "created_${Random.nextInt(1000, 9999)}",
            name = groupName,
            code = code,
            recipes = emptyList(),
        )
        _uiState.update {
            it.copy(
                groups = it.groups + created,
                selectedGroupId = created.id,
                infoMessage = "Group created. Code: $code",
                dialogMode = null,
                createNameInput = "",
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
                        ),
                    )
                }
            }
            _uiState.update { it.copy(groups = mappedGroups) }
        }
    }

    private fun defaultState(): GroupsUiState {
        val personalRecipes = listOf(
            SharedRecipe("Tomato Omelette", "Soft omelette with tomatoes and herbs.", "You", emptyList()),
            SharedRecipe("Chicken Rice Bowl", "Rice bowl with chicken and green vegetables.", "You", emptyList()),
        )
        val groups = listOf(
            RecipeGroup(
                id = "personal",
                name = "Your recipes",
                code = null,
                recipes = personalRecipes,
                isPersonal = true,
            ),
            RecipeGroup(
                id = "g1",
                name = "Flatmates",
                code = "A7K2P1",
                recipes = listOf(
                    SharedRecipe("Pasta Carbonara", "Classic creamy pasta with bacon and parmesan.", "Anton", listOf("2 eggs")),
                    SharedRecipe("Veggie Stir Fry", "Fast stir fry with peppers and soy sauce.", "Mira", listOf("1 red pepper")),
                ),
            ),
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

