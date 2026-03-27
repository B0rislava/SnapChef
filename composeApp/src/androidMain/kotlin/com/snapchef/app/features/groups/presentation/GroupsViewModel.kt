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
    private val currentUser = GroupMember(username = "You", avatarSeed = "you")
    private val discoverableGroups = listOf(
        RecipeGroup(
            id = "discover_kitchen",
            name = "Kitchen Crew",
            code = "K9M4Q2",
            recipes = listOf(
                SharedRecipe(
                    title = "Shared Salad Bowl",
                    description = "A quick group salad everyone can customize.",
                    ownerName = "Niki",
                    missingItems = listOf("Avocado"),
                    availableItems = listOf("Lettuce", "Tomatoes", "Cucumber", "Olive oil"),
                    instructions = listOf(
                        "Chop all vegetables.",
                        "Mix dressing in a separate bowl.",
                        "Toss and serve immediately.",
                    ),
                )
            ),
            ownerUsername = "Niki",
            members = listOf(
                GroupMember("Niki"),
                GroupMember("Sani"),
                GroupMember("Viki"),
            ),
        ),
        RecipeGroup(
            id = "discover_fit",
            name = "Fit Meals",
            code = "F3T8L6",
            recipes = emptyList(),
            ownerUsername = "Alex",
            members = listOf(
                GroupMember("Alex"),
                GroupMember("Mira"),
            ),
        ),
    )

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

    fun selectGroup(id: String) = _uiState.update { it.copy(selectedGroupId = id, expanded = false) }
    fun openDialog(mode: GroupDialogMode) = _uiState.update { it.copy(dialogMode = mode) }
    fun closeDialog() = _uiState.update { it.copy(dialogMode = null) }
    fun setJoinCodeInput(value: String) = _uiState.update { it.copy(joinCodeInput = value) }
    fun setCreateNameInput(value: String) = _uiState.update { it.copy(createNameInput = value) }
    fun openRecipe(recipe: SharedRecipe) = _uiState.update { it.copy(selectedRecipe = recipe) }
    fun closeRecipeDetails() = _uiState.update { it.copy(selectedRecipe = null) }
    fun clearInfoMessage() = _uiState.update { it.copy(infoMessage = null) }

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

        val source = discoverableGroups.firstOrNull { it.code == code }
        if (source == null) {
            _uiState.update {
                it.copy(
                    infoMessage = "Group with code $code was not found.",
                    dialogMode = null,
                    joinCodeInput = "",
                )
            }
            return
        }

        val joined = source.copy(
            id = "joined_${Random.nextInt(1000, 9999)}",
            members = (source.members + currentUser).distinctBy { it.username.lowercase() },
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
            ownerUsername = currentUser.username,
            members = listOf(currentUser),
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

    fun renameSelectedGroup(newNameRaw: String) {
        val state = _uiState.value
        val selected = state.groups.firstOrNull { it.id == state.selectedGroupId } ?: return
        val newName = newNameRaw.trim()

        if (newName.isBlank()) {
            _uiState.update { it.copy(infoMessage = "Group name cannot be empty.") }
            return
        }

        if (!selected.ownerUsername.equals(currentUser.username, ignoreCase = true)) {
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

        if (!selected.ownerUsername.equals(currentUser.username, ignoreCase = true)) {
            _uiState.update { it.copy(infoMessage = "Only the group admin can remove members.") }
            return
        }

        if (username.equals(currentUser.username, ignoreCase = true) ||
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

        if (selected.ownerUsername.equals(currentUser.username, ignoreCase = true)) {
            _uiState.update { it.copy(infoMessage = "Admins cannot leave their own group. Delete it instead.") }
            return
        }

        val updatedGroups = state.groups.filterNot { it.id == selected.id }
        _uiState.update {
            it.copy(
                groups = updatedGroups,
                selectedGroupId = "g1",
                infoMessage = "You left ${selected.name}.",
            )
        }
    }

    fun deleteSelectedGroup() {
        val state = _uiState.value
        val selected = state.groups.firstOrNull { it.id == state.selectedGroupId } ?: return

        if (selected.isPersonal) {
            _uiState.update { it.copy(infoMessage = "Personal group cannot be deleted.") }
            return
        }

        if (!selected.ownerUsername.equals(currentUser.username, ignoreCase = true)) {
            _uiState.update { it.copy(infoMessage = "Only the group admin can delete this group.") }
            return
        }

        val updatedGroups = state.groups.filterNot { it.id == selected.id }
        _uiState.update {
            it.copy(
                groups = updatedGroups,
                selectedGroupId = "g1",
                infoMessage = "Group ${selected.name} deleted.",
            )
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
                ownerUsername = currentUser.username,
                members = listOf(currentUser),
                isPersonal = true,
            ),
            RecipeGroup(
                id = "g1",
                name = "Flatmates",
                code = "A7K2P1",
                recipes = flatmatesBaseRecipes(),
                ownerUsername = "Anton",
                members = listOf(
                    GroupMember("Anton"),
                    GroupMember("Mira"),
                    currentUser,
                ),
            ),
            RecipeGroup(
                id = "g2",
                name = "Meal Planners",
                code = "M8L5R3",
                recipes = listOf(
                    SharedRecipe(
                        title = "Mediterranean Wraps",
                        description = "Fresh wraps for a quick group lunch.",
                        ownerName = "You",
                        missingItems = listOf("Hummus"),
                        availableItems = listOf("Tortillas", "Cucumber", "Tomatoes", "Feta", "Olive oil"),
                        instructions = listOf(
                            "Slice vegetables and prepare fillings.",
                            "Warm tortillas briefly in a pan.",
                            "Assemble wraps and drizzle olive oil.",
                        ),
                    ),
                    SharedRecipe(
                        title = "One-Pot Lentil Curry",
                        description = "Comforting curry that is easy to batch-cook.",
                        ownerName = "Sani",
                        missingItems = listOf("Coconut milk"),
                        availableItems = listOf("Lentils", "Onion", "Garlic", "Curry powder"),
                        instructions = listOf(
                            "Saute onion and garlic until soft.",
                            "Add lentils, spices, and water.",
                            "Simmer until thick and creamy.",
                        ),
                    ),
                ),
                ownerUsername = currentUser.username,
                members = listOf(
                    currentUser,
                    GroupMember("Sani"),
                    GroupMember("Niki"),
                    GroupMember("Viktor"),
                ),
            ),
        )
        return GroupsUiState(groups = groups, selectedGroupId = "g1")
    }

    private fun personalBaseRecipes(): List<SharedRecipe> {
        return listOf(
            SharedRecipe(
                "Tomato Omelette",
                "Soft omelette with tomatoes and herbs.",
                "You",
                missingItems = emptyList(),
                availableItems = listOf("Eggs", "Tomatoes", "Salt", "Pepper", "Oil", "Fresh Herbs"),
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
                missingItems = emptyList(),
                availableItems = listOf("Chicken breast", "Rice", "Green onion", "Soy sauce", "Sesame seeds"),
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
