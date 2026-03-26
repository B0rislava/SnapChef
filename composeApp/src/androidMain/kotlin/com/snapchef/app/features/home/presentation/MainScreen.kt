package com.snapchef.app.features.home.presentation

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.snapchef.app.core.presentation.components.MainTab
import com.snapchef.app.core.presentation.components.SnapChefBottomBar
import com.snapchef.app.core.theme.GreenSecondary
import com.snapchef.app.features.groups.presentation.GroupsScreen
import com.snapchef.app.features.profile.presentation.EditProfileScreen
import com.snapchef.app.features.profile.presentation.ProfileScreen

@Composable
fun MainScreen() {
    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    
    // Profile State
    var userName by remember { mutableStateOf("John Doe") }
    var userEmail by remember { mutableStateOf("john.doe@snapchef.app") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var isEditingProfile by remember { mutableStateOf(false) }

    // Recipe Generation State
    var activeRecipeIngredients by remember { mutableStateOf<List<String>?>(null) }

    Crossfade(targetState = activeRecipeIngredients, label = "recipe_results_crossfade") { ingredients ->
        if (ingredients != null) {
            RecipeResultsScreen(
                ingredients = ingredients,
                onBack = { activeRecipeIngredients = null }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GreenSecondary)
            ) {
                
                AnimatedContent(
                    targetState = currentTab,
                    label = "main_content",
                    modifier = Modifier.fillMaxSize()
                ) { tab ->
                    when (tab) {
                        MainTab.HOME -> {
                            HomeScreen(
                                onGenerateRecipes = { activeRecipeIngredients = it }
                            )
                        }
                        MainTab.RECIPES -> {
                            GroupsScreen()
                        }
                        MainTab.PROFILE -> {
                            Crossfade(targetState = isEditingProfile, label = "profile_edit_crossfade") { editing ->
                                if (editing) {
                                    EditProfileScreen(
                                        userName = userName,
                                        userEmail = userEmail,
                                        profileImageUri = profileImageUri,
                                        onPickImage = { profileImageUri = it },
                                        onSave = { newName, newEmail ->
                                            userName = newName
                                            userEmail = newEmail
                                            isEditingProfile = false
                                        },
                                        onCancel = { isEditingProfile = false }
                                    )
                                } else {
                                    ProfileScreen(
                                        userName = userName,
                                        userEmail = userEmail,
                                        profileImageUri = profileImageUri,
                                        onBack = { currentTab = MainTab.HOME },
                                        onLogout = { /* Handle global logout */ },
                                        onDeleteAccount = { /* Handle account deletion */ },
                                        onEditProfile = { isEditingProfile = true }
                                    )
                                }
                            }
                        }
                    }
                } 

                if (!isEditingProfile || currentTab != MainTab.PROFILE) {
                    SnapChefBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { 
                            currentTab = it 
                            if (it != MainTab.PROFILE) isEditingProfile = false
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                    )
                }
            } 
        }
    } 
}
