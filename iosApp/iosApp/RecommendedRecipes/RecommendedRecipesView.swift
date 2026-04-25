import SwiftUI

struct RecommendedRecipesView: View {
    @StateObject private var viewModel = RecommendedRecipesViewModel()
    @EnvironmentObject private var appFlow: AppFlowState
    @EnvironmentObject private var groupsViewModel: GroupsViewModel
    @EnvironmentObject private var mainChrome: MainChromeState
    @ObservedObject private var favoriteStore = FavoritesStore.shared
    @State private var showGroupShareSheet = false

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

            if let recipe = viewModel.uiState.openedRecipe {
                RecipeDetailView(
                    recipe: recipe,
                    checkedIngredients: $viewModel.uiState.checkedIngredients,
                    infoMessage: viewModel.uiState.infoMessage,
                    isFavorite: favoriteStore.isFavorite(id: recipe.id),
                    onBack: { viewModel.closeRecipe() },
                    onToggle: { viewModel.toggleIngredient($0, checked: $1) },
                    onSave: { viewModel.saveCurrentRecipe() },
                    onShare: { showGroupShareSheet = true },
                    onToggleFavorite: { viewModel.toggleFavoriteCurrent() }
                )
                .transition(.move(edge: .trailing).combined(with: .opacity))
            } else {
                recipeListContent
                    .transition(.move(edge: .leading).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.3), value: viewModel.uiState.openedRecipe)
        .onChange(of: viewModel.uiState.openedRecipe) { _, r in
            mainChrome.hideTabBar = (r != nil)
        }
        .onAppear {
            if viewModel.uiState.openedRecipe != nil { mainChrome.hideTabBar = true }
            viewModel.loadRecommendations(flow: appFlow)
        }
        .onChange(of: appFlow.recipeSessionVersion) { _, _ in
            viewModel.loadRecommendations(flow: appFlow)
        }
        .sheet(isPresented: $showGroupShareSheet) {
            if let r = viewModel.uiState.openedRecipe {
                GroupSelectionSheet(viewModel: groupsViewModel) { group in
                    groupsViewModel.shareRecipeToGroup(r.toSharedRecipe(), to: group)
                    showGroupShareSheet = false
                }
            } else {
                VStack { Text("No recipe to share") }
                    .onAppear { showGroupShareSheet = false }
            }
        }
        .overlay(alignment: .top) {
            if let msg = groupsViewModel.infoMessage, viewModel.uiState.openedRecipe != nil {
                Text(msg)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .background(groupsViewModel.isError ? Color.red.opacity(0.9) : Color.greenPrimary)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding(.top, 8)
                    .padding(.horizontal, 16)
            }
        }
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

                if viewModel.uiState.isLoading {
                    HStack { Spacer(); ProgressView().tint(Color.greenPrimary).scaleEffect(1.1); Spacer() }
                        .padding(.vertical, 24)
                } else if let err = viewModel.uiState.errorMessage, viewModel.uiState.recipes.isEmpty {
                    Text(err)
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(Color.greenOnBackground.opacity(0.85))
                        .padding(20)
                        .frame(maxWidth: .infinity, alignment: .center)
                }

                if !favoriteStore.items.isEmpty {
                    VStack(alignment: .leading, spacing: 10) {
                        Text("Favorites")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(Color.greenPrimary)
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 12) {
                                ForEach(favoriteStore.items) { rec in
                                    Button {
                                        viewModel.openFavorite(rec)
                                    } label: {
                                        VStack(alignment: .leading, spacing: 4) {
                                            Text(rec.title)
                                                .font(.system(size: 14, weight: .bold))
                                                .foregroundColor(Color.greenOnBackground)
                                                .lineLimit(2)
                                            Text("♥")
                                                .font(.system(size: 12))
                                                .foregroundColor(Color.greenPrimary)
                                        }
                                        .padding(14)
                                        .frame(width: 160, alignment: .leading)
                                        .background(Color.white)
                                        .clipShape(RoundedRectangle(cornerRadius: 16))
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 16)
                                                .stroke(Color.greenSecondary, lineWidth: 1.2)
                                        )
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                        }
                    }
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
                                Text(
                                    appFlow.lastSessionId == nil
                                        ? "AI picks from your history and tastes"
                                        : "\(viewModel.uiState.recipes.count) ideas · includes your latest scan"
                                )
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
                    Text("Choose your next meal")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(Color.greenPrimary)
                        .padding(.bottom, 16)

                    if viewModel.uiState.recipes.isEmpty, !viewModel.uiState.isLoading, viewModel.uiState.errorMessage == nil {
                        Text("Pull to refresh, or run a Home scan to tailor the list to your kitchen.")
                            .font(.system(size: 15))
                            .foregroundColor(Color.greenOnBackground.opacity(0.65))
                            .padding(.vertical, 8)
                    }

                    VStack(spacing: 12) {
                        ForEach(Array(viewModel.uiState.recipes.enumerated()), id: \.element.id) { index, recipe in
                            RecipeCard(recipe: recipe) {
                                viewModel.openRecipeIndex(index)
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
        .refreshable { await viewModel.refreshRecommendations(flow: appFlow) }
    }
}

private struct RecipeCard: View {
    let recipe: RecommendedRecipeItem
    let onPress: () -> Void

    var body: some View {
        Button(action: onPress) {
            VStack(alignment: .leading, spacing: 12) {

                HStack {
                    RecipePill(isQuick: recipe.isQuick)
                    Spacer()
                    if let m = recipe.minutes {
                        HStack(spacing: 4) {
                            Image(systemName: "clock")
                                .font(.system(size: 12))
                                .foregroundColor(Color.greenPrimary)
                            Text("\(m) min")
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundColor(Color.greenOnBackground.opacity(0.75))
                        }
                    } else {
                        HStack(spacing: 4) {
                            Image(systemName: "clock")
                                .font(.system(size: 12))
                                .foregroundColor(Color.greenPrimary)
                            Text(recipe.isQuick ? "15-20 min" : "30+ min")
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundColor(Color.greenOnBackground.opacity(0.75))
                        }
                    }
                }

                Text(recipe.title)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(Color.greenOnBackground)

                Text(recipe.description)
                    .font(.system(size: 14))
                    .foregroundColor(Color.greenOnBackground.opacity(0.72))

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
