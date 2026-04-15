import SwiftUI

@MainActor
final class RecipeStore: ObservableObject {
    static let shared = RecipeStore()
    
    @Published private(set) var personalRecipes: [SharedRecipe] = []
    @Published private(set) var sharedRecipes: [SharedRecipe] = []
    
    private init() {}
    
    func addPersonalRecipe(_ recipe: SharedRecipe) {
        personalRecipes.insert(recipe, at: 0)
    }
    
    func addSharedRecipe(_ recipe: SharedRecipe) {
        sharedRecipes.insert(recipe, at: 0)
    }
}
