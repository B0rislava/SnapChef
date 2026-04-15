package com.snapchef.app.features.home.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.snapchef.app.core.presentation.components.MainTab
import com.snapchef.app.core.presentation.components.SnapChefBottomBar
import com.snapchef.app.core.theme.GreenSecondary
import com.snapchef.app.features.groups.presentation.RecipesScreen
import com.snapchef.app.features.groups.presentation.GroupsScreen
import com.snapchef.app.features.profile.presentation.EditProfileScreen
import com.snapchef.app.features.profile.presentation.ProfileScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    viewModel: MainViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isViewingGroupRecipeDetails by remember { mutableStateOf(false) }
    var isViewingRecommendedRecipeDetails by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.currentTab) {
        if (uiState.currentTab != MainTab.RECIPES) isViewingGroupRecipeDetails = false
        if (uiState.currentTab != MainTab.RECOMMENDED) isViewingRecommendedRecipeDetails = false
    }

    Crossfade(targetState = uiState.activeRecipeSession, label = "recipe_results_crossfade") { session ->
        if (session != null) {
            RecipeResultsScreen(
                sessionId = session.sessionId,
                ingredients = session.ingredients,
                onBack = viewModel::closeRecipeResults,
                onSaveRecipe = viewModel::saveGeneratedRecipe,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GreenSecondary)
            ) {
                
                AnimatedContent(
                    targetState = uiState.currentTab,
                    label = "main_content",
                    modifier = Modifier.fillMaxSize()
                ) { tab ->
                    when (tab) {
                        MainTab.HOME -> {
                            HomeScreen(
                                onGenerateRecipes = viewModel::openRecipeResults,
                                isCameraActive = uiState.isCameraActive,
                                onCameraActiveChanged = viewModel::setCameraActive,
                            )
                        }
                        MainTab.RECIPES -> {
                            RecipesScreen(
                                onDetailsVisibilityChanged = { isViewingGroupRecipeDetails = it }
                            )
                        }
                        MainTab.GROUPS -> {
                            GroupsScreen()
                        }
                        MainTab.RECOMMENDED -> {
                            RecommendedRecipesScreen(
                                onSaveRecipe = viewModel::saveGeneratedRecipe,
                                onDetailsVisibilityChanged = { isViewingRecommendedRecipeDetails = it },
                            )
                        }
                        MainTab.PROFILE -> {
                            Crossfade(targetState = uiState.isEditingProfile, label = "profile_edit_crossfade") { editing ->
                                if (editing) {
                                    EditProfileScreen(
                                        userName = uiState.userName,
                                        userEmail = uiState.userEmail,
                                        profileImageUri = uiState.profileImageUri,
                                        onPickImage = viewModel::setProfileImage,
                                        onSave = viewModel::saveProfile,
                                        onCancel = viewModel::cancelEditProfile,
                                    )
                                } else {
                                    ProfileScreen(
                                        userName = uiState.userName,
                                        userEmail = uiState.userEmail,
                                        profileImageUri = uiState.profileImageUri,
                                        inventoryItems = uiState.inventoryItems,
                                        onLogout = viewModel::logout,
                                        onDeleteAccount = viewModel::deleteAccount,
                                        onEditProfile = viewModel::startEditProfile
                                    )
                                }
                            }
                        }
                    }
                } 

                if (
                    !uiState.isEditingProfile &&
                    !uiState.isCameraActive &&
                    !isViewingGroupRecipeDetails &&
                    !isViewingRecommendedRecipeDetails
                ) {

                    SnapChefBottomBar(
                        currentTab = uiState.currentTab,
                        onTabSelected = viewModel::selectTab,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                    )
                }
            } 
        }
    } 
}
