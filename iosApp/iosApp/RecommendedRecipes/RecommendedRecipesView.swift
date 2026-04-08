//
//  RecommendedRecipesView.swift
//  iosApp
//
//  Created by gergana on 4/8/26.
//

import SwiftUI

struct RecommendedRecipesView: View {
    @StateObject private var viewModel = RecommendedRecipesViewModel()

    var body: some View {
        ZStack(alignment: .top) {
            LinearGradient(
                colors: [Color.greenSecondary.opacity(0.55), Color.greenBackground],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            Circle()
                .fill(Color.greenPrimary.opacity(0.10))
                .frame(width: 240, height: 240)
                .offset(x: UIScreen.main.bounds.width - 60, y: -40)

            if let idx = viewModel.uiState.openedRecipeIdx,
               let recipe = viewModel.uiState.recipes[safe: idx] {
                // Detail view
                RecipeDetailView(
                    recipe: recipe,
                    checkedIngredients: $viewModel.uiState.checkedIngredients,
                    infoMessage: viewModel.uiState.infoMessage,
                    onBack: { viewModel.closeRecipe() },
                    onToggle: { viewModel.toggleIngredient($0, checked: $1) },
                    onSave: {
                        viewModel.setInfoMessage(NSLocalizedString("saved", comment: ""))
                    },
                    onShare:            {
                        viewModel.setInfoMessage(NSLocalizedString("shared_to_your_group", comment: ""))
                    }
                )
                .transition(.move(edge: .trailing).combined(with: .opacity))
            } else {
                // List view
                recipeListContent
                    .transition(.move(edge: .leading).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.3), value: viewModel.uiState.openedRecipeIdx)
    }

    private var recipeListContent: some View {
        ScrollView(showsIndicators: false) {
            VStack(alignment: .leading, spacing: 16) {
                Spacer().frame(height: 24)

                VStack(alignment: .leading, spacing: 4) {
                    Text(NSLocalizedString("recommended_recipes", comment: ""))
                        .font(.system(size: 28, weight: .heavy))
                        .foregroundColor(Color.greenPrimary)
                    Text(NSLocalizedString("fresh_picks", comment: ""))
                        .font(.system(size: 15))
                        .foregroundColor(Color.greenOnBackground.opacity(0.75))
                }

                RoundedRectangle(cornerRadius: 20)
                    .fill(Color.greenPrimary)
                    .frame(maxWidth: .infinity)
                    .frame(height: 72)
                    .overlay(
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(NSLocalizedString("todays_picks", comment: ""))
                                    .font(.system(size: 13, weight: .semibold))
                                    .foregroundColor(.white.opacity(0.9))
                                Text("\(viewModel.uiState.recipes.count) curated recipes")
                                    .font(.system(size: 17, weight: .bold))
                                    .foregroundColor(.white)
                            }
                            Spacer()
                            Image(systemName: "book.fill")
                                .font(.system(size: 24))
                                .foregroundColor(.white)
                        }
                        .padding(.horizontal, 20)
                    )

                VStack(alignment: .leading, spacing: 0) {
                    Text(NSLocalizedString("choose_yours_next_meal", comment: ""))
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(Color.greenPrimary)
                        .padding(.bottom, 16)

                    VStack(spacing: 12) {
                        ForEach(Array(viewModel.uiState.recipes.enumerated()), id: \.element.id) { index, recipe in
                            RecipeCard(recipe: recipe) {
                                viewModel.openRecipe(index: index)
                            }
                        }
                    }
                }
                .padding(24)
                .background(Color.white)
                .clipShape(RoundedRectangle(cornerRadius: 24))
                .shadow(color: .black.opacity(0.07), radius: 8, x: 0, y: 4)

                Spacer().frame(height: 96)
            }
            .padding(.horizontal, 24)
        }
    }
}

//Recipe Card
private struct RecipeCard: View {
    let recipe: RecommendedRecipeItem
    let onPress: () -> Void

    @State private var isPressed = false

    var body: some View {
        Button(action: onPress) {
            VStack(alignment: .leading, spacing: 12) {

                // time
                HStack {
                    RecipePill(isQuick: recipe.isQuick)
                    Spacer()
                    HStack(spacing: 4) {
                        Image(systemName: "clock")
                            .font(.system(size: 12))
                            .foregroundColor(Color.greenPrimary)
                        Text(recipe.isQuick ? "15-20 min" : "30-40 min")
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundColor(Color.greenOnBackground.opacity(0.75))
                    }
                }

                // Title + description
                Text(recipe.title)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(Color.greenOnBackground)

                Text(recipe.description)
                    .font(.system(size: 14))
                    .foregroundColor(Color.greenOnBackground.opacity(0.72))

                // Bottom row
                HStack {
                    Text("\(recipe.ingredients.count) ingredients")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(Color.greenPrimary)
                    Spacer()
                    Image(systemName: "chevron.right")
                        .font(.system(size: 13))
                        .foregroundColor(Color.greenPrimary)
                }
            }
            .padding(16)
            .background(Color.greenBackground)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(Color.greenSecondary, lineWidth: 1.5)
            )
        }
        .buttonStyle(RecipeBouncyButtonStyle())
    }
}

struct RecipePill: View {
    let isQuick: Bool

    var body: some View {
        Text(isQuick ? "Quick pick" : "Chef choice")
            .font(.system(size: 13, weight: .semibold))
            .foregroundColor(isQuick ? .white : Color.greenOnBackground)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(isQuick ? Color.greenPrimary : Color.greenSecondary)
            .clipShape(Capsule())
    }
}

struct RecipeBouncyButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.98 : 1.0)
            .animation(.spring(response: 0.25, dampingFraction: 0.65), value: configuration.isPressed)
    }
}

private extension Array {
    subscript(safe index: Int) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}
