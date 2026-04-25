import Foundation
import Shared

enum SavedRecipesCloudSync {
    @MainActor
    static func run() {
        guard AuthManager.shared.isLoggedIn() else { return }
        let api = SnapChefServiceLocator.shared.authApiService
        let saved = RecipeStore.shared.personalRecipes
        if saved.isEmpty { return }
        Task {
            var cloudCatalog = Set<Int>()
            var cloudSession = Set<Int>()
            if let cat = try? await api.listCatalogFavoriteRecipes() {
                cloudCatalog = Set(cat.map { Int($0.id) })
            }
            if let sess = try? await api.listFavoriteSessionRecipes(limit: 50, offset: 0) {
                cloudSession = Set(sess.map { Int($0.id) })
            }
            for r in saved {
                if let cid = r.catalogRecipeId, !cloudCatalog.contains(cid) {
                    try? await api.starCatalogRecipe(recipeId: Int32(cid))
                    cloudCatalog.insert(cid)
                }
                if let sid = r.sessionRecipeId, !cloudSession.contains(sid) {
                    _ = try? await api.favoriteSessionRecipe(sessionRecipeId: Int32(sid))
                    cloudSession.insert(sid)
                }
            }
        }
    }
}
