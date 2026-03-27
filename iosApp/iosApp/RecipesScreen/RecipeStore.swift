//
//  RecipeStore.swift
//  iosApp
//
//  Created by gergana on 3/27/26.
//
import Combine

final class RecipeStore: ObservableObject {

    static let shared = RecipeStore()
    private init() {}

    @Published private(set) var personalRecipes: [SharedRecipe] = []
    @Published private(set) var sharedRecipes: [SharedRecipe] = []

    func addPersonalRecipe(_ recipe: SharedRecipe) {
        personalRecipes.insert(recipe, at: 0)
    }

    func addSharedRecipe(_ recipe: SharedRecipe) {
        sharedRecipes.insert(recipe, at: 0)
    }
}
