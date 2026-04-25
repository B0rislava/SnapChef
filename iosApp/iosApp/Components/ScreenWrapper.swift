
import SwiftUI

struct ScreenWrapper: View {
    @State private var currentTab: MainTab = .home
    @StateObject private var appFlow = AppFlowState()
    @StateObject private var groupsViewModel = GroupsViewModel()
    @StateObject private var mainChrome = MainChromeState()

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.greenSecondary
                .ignoresSafeArea()

            ZStack {
                HomeView(
                    onSessionReady: { sid, ings in
                        appFlow.notifyRecipeSession(id: sid, ingredients: ings)
                        withAnimation { currentTab = .recommended }
                    }
                )
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .opacity(currentTab == .home ? 1 : 0)
                    .environmentObject(appFlow)
                
                RecipesView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .opacity(currentTab == .recipes ? 1 : 0)
                    .environmentObject(groupsViewModel)
                    
                GroupsView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .opacity(currentTab == .groups ? 1 : 0)
                    .environmentObject(groupsViewModel)
                
                RecommendedRecipesView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .opacity(currentTab == .recommended ? 1 : 0)
                    .environmentObject(appFlow)
                    .environmentObject(groupsViewModel)

                ProfileView(
                    onBack: { currentTab = .home },
                    onLogout: { SessionManager.shared.logout() },
                    onDeleteAccount: { SessionManager.shared.logout() }
                )
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .opacity(currentTab == .profile ? 1 : 0)
            }
            .environmentObject(mainChrome)

            if !mainChrome.hideTabBar {
                SnapChefBottomBar(currentTab: $currentTab)
            }
        }
        .onChange(of: currentTab) { _, _ in
            mainChrome.hideTabBar = false
        }
        .onAppear {
            Task { @MainActor in
                RecipeStore.shared.reloadFromStorageForCurrentUser()
                SavedRecipesCloudSync.run()
            }
        }
        .ignoresSafeArea(edges: .bottom)
    }
}
