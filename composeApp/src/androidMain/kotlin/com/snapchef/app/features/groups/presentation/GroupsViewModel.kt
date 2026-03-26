package com.snapchef.app.features.groups.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
)

class GroupsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(defaultState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            RecipeStore.personalRecipes.collect { savedRecipes ->
                _uiState.update { state ->
                    val updatedGroups = state.groups.map { group ->
                        if (group.id == "personal") {
                            group.copy(recipes = personalBaseRecipes() + savedRecipes)
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
                        if (group.id == "g1") {
                            group.copy(recipes = flatmatesBaseRecipes() + shared)
                        } else {
                            group
                        }
                    }
                    state.copy(groups = updatedGroups)
                }
            }
        }
    }

    fun setExpanded(value: Boolean) = _uiState.update { it.copy(expanded = value) }
    fun selectGroup(id: String) = _uiState.update { it.copy(selectedGroupId = id, expanded = false) }
    fun openDialog(mode: GroupDialogMode) = _uiState.update { it.copy(dialogMode = mode) }
    fun closeDialog() = _uiState.update { it.copy(dialogMode = null) }
    fun setJoinCodeInput(value: String) = _uiState.update { it.copy(joinCodeInput = value) }
    fun setCreateNameInput(value: String) = _uiState.update { it.copy(createNameInput = value) }
    fun openRecipe(recipe: SharedRecipe) = _uiState.update { it.copy(selectedRecipe = recipe) }
    fun closeRecipeDetails() = _uiState.update { it.copy(selectedRecipe = null) }
    fun updatePerishableFreshness(productName: String, freshness: Float) {
        val state = _uiState.value
        val selected = state.selectedRecipe ?: return
        val updatedSelected = selected.copy(
            perishableProducts = selected.perishableProducts.map { item ->
                if (item.name == productName) item.copy(freshness = freshness.coerceIn(0f, 1f)) else item
            },
        )
        val updatedGroups = state.groups.map { group ->
            group.copy(
                recipes = group.recipes.map { recipe ->
                    if (recipe.title == selected.title && recipe.ownerName == selected.ownerName) {
                        updatedSelected
                    } else {
                        recipe
                    }
                },
            )
        }
        _uiState.update { it.copy(groups = updatedGroups, selectedRecipe = updatedSelected) }
    }

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
                    instructions = listOf(
                        "Boil water in a medium pot.",
                        "Add vegetables and simmer for 10 minutes.",
                        "Season and serve warm.",
                    ),
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
                        ),
                    )
                }
            }
            _uiState.update { it.copy(groups = mappedGroups) }
        }
    }

    private fun defaultState(): GroupsUiState {
        val personalRecipes = personalBaseRecipes()
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
                recipes = flatmatesBaseRecipes(),
            ),
        )
        return GroupsUiState(groups = groups, selectedGroupId = "personal")
    }

    private fun personalBaseRecipes(): List<SharedRecipe> {
        return listOf(
            SharedRecipe(
                "Tomato Omelette",
                "Soft omelette with tomatoes and herbs.",
                "You",
                emptyList(),
                instructions = listOf(
                    "Whisk eggs with salt and pepper.",
                    "Cook tomatoes for 2 minutes.",
                    "Pour eggs and cook until set.",
                ),
            ),
            SharedRecipe(
                "Chicken Rice Bowl",
                "Rice bowl with chicken and green vegetables.",
                "You",
                emptyList(),
                instructions = listOf(
                    "Cook rice and keep warm.",
                    "Saute chicken until fully cooked.",
                    "Add vegetables and stir for 3 minutes.",
                    "Serve over rice.",
                ),
                perishableProducts = listOf(
                    PerishableProduct("Chicken breast", maxFreshDays = 2, freshness = 0.65f),
                    PerishableProduct("Green onion", maxFreshDays = 4, freshness = 0.45f),
                ),
            ),
        )
    }

    private fun flatmatesBaseRecipes(): List<SharedRecipe> {
        return listOf(
            SharedRecipe(
                "Pasta Carbonara",
                "Classic creamy pasta with bacon and parmesan.",
                "Anton",
                listOf("2 eggs"),
                instructions = listOf(
                    "Cook pasta in salted water.",
                    "Fry bacon until crisp.",
                    "Mix eggs and cheese in a bowl.",
                    "Combine pasta with bacon and egg mix off the heat.",
                ),
                perishableProducts = listOf(
                    PerishableProduct("Bacon", maxFreshDays = 3, freshness = 0.18f),
                    PerishableProduct("Eggs", maxFreshDays = 6, freshness = 0.42f),
                ),
            ),
            SharedRecipe(
                "Veggie Stir Fry",
                "Fast stir fry with peppers and soy sauce.",
                "Mira",
                listOf("1 red pepper"),
                instructions = listOf(
                    "Chop all vegetables evenly.",
                    "Heat wok and add oil.",
                    "Stir-fry vegetables for 4-5 minutes.",
                    "Add soy sauce and serve.",
                ),
                perishableProducts = listOf(
                    PerishableProduct("Bell pepper", maxFreshDays = 5, freshness = 0.06f),
                    PerishableProduct("Mushrooms", maxFreshDays = 2, freshness = 0.0f),
                ),
            ),
        )
    }
}

private fun generateGroupCode(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return buildString {
        repeat(6) { append(chars[Random.nextInt(chars.length)]) }
    }
}

