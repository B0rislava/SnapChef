//
//  RecommendedRecipesViewModel.swift
//  iosApp
//
//  Created by gergana on 4/8/26.
//

import Foundation

struct RecommendedRecipeItem: Identifiable {
    let id          = UUID()
    let title:       String
    let description: String
    let instructions: [String]
    let ingredients:  [String]
    let isQuick:      Bool
}

struct RecommendedRecipesUiState {
    var recipes:            [RecommendedRecipeItem] = []
    var openedRecipeIdx:    Int?                    = nil
    var checkedIngredients: [String: Bool]          = [:]
    var infoMessage:        String?                 = nil
}

@MainActor
final class RecommendedRecipesViewModel: ObservableObject {
    @Published private(set) var uiState = RecommendedRecipesUiState(
        recipes: sampleRecipes()
    )

    func openRecipe(index: Int) {
        guard index < uiState.recipes.count else { return }
        let recipe = uiState.recipes[index]
        uiState.openedRecipeIdx    = index
        uiState.checkedIngredients = Dictionary(uniqueKeysWithValues: recipe.ingredients.map { ($0, true) })
        uiState.infoMessage        = nil
    }

    func closeRecipe() {
        uiState.openedRecipeIdx    = nil
        uiState.checkedIngredients = [:]
        uiState.infoMessage        = nil
    }

    func toggleIngredient(_ ingredient: String, checked: Bool) {
        uiState.checkedIngredients[ingredient] = checked
    }

    func setInfoMessage(_ value: String?) {
        uiState.infoMessage = value
    }
}

private func sampleRecipes() -> [RecommendedRecipeItem] {
    [
        RecommendedRecipeItem(
            title:       "Creamy Mushroom Pasta",
            description: "Quick creamy pasta for weeknights.",
            instructions: [
                "Boil pasta in salted water until al dente.",
                "Cook mushrooms and garlic in olive oil until fragrant.",
                "Stir in cream (or a dairy-free alternative) and season to taste.",
                "Toss pasta with the sauce and finish with parmesan."
            ],
            ingredients: ["Pasta", "Mushrooms", "Garlic", "Cream", "Parmesan", "Olive oil"],
            isQuick: true
        ),
        RecommendedRecipeItem(
            title:       "Chicken Veggie Bowl",
            description: "Balanced protein bowl with fresh vegetables.",
            instructions: [
                "Cook rice (or use leftover rice) and keep warm.",
                "Sear chicken until golden and cooked through.",
                "Quick-saute bell pepper and onions.",
                "Combine everything with soy sauce and serve."
            ],
            ingredients: ["Chicken breast", "Rice", "Bell pepper", "Soy sauce", "Green onion", "Onion"],
            isQuick: true
        ),
        RecommendedRecipeItem(
            title:       "Spicy Chickpea Tacos",
            description: "Smoky, spicy chickpeas with crunchy toppings.",
            instructions: [
                "Saute onion and garlic, then toast spices for 30 seconds.",
                "Simmer chickpeas until saucy and flavorful.",
                "Warm tortillas and assemble with toppings.",
                "Finish with lime and a creamy drizzle."
            ],
            ingredients: ["Chickpeas", "Tortillas", "Onion", "Garlic", "Cumin", "Chili powder", "Lime", "Yogurt"],
            isQuick: false
        ),
        RecommendedRecipeItem(
            title:       "Lemon Herb Salmon",
            description: "Bright lemon-herb salmon with a buttery finish.",
            instructions: [
                "Preheat oven and season salmon with salt and pepper.",
                "Bake until just flaky.",
                "Mix butter (or olive oil) with lemon zest, juice, and herbs.",
                "Pour over salmon and serve with greens."
            ],
            ingredients: ["Salmon", "Lemon", "Garlic", "Butter", "Dill", "Parsley", "Olive oil"],
            isQuick: true
        ),
        RecommendedRecipeItem(
            title:       "Tofu Stir-Fry",
            description: "Crispy tofu with colorful vegetables and a savory sauce.",
            instructions: [
                "Press tofu, then pan-sear until crisp.",
                "Stir-fry vegetables on high heat.",
                "Add sauce (soy + ginger + garlic) and toss until glossy.",
                "Serve over rice or noodles."
            ],
            ingredients: ["Tofu", "Broccoli", "Carrot", "Soy sauce", "Ginger", "Garlic", "Cornstarch", "Sesame oil"],
            isQuick: true
        ),
        RecommendedRecipeItem(
            title:       "Greek Quinoa Salad",
            description: "Fresh quinoa salad with cucumber, feta, and herbs.",
            instructions: [
                "Cook quinoa and let it cool slightly.",
                "Chop cucumber, tomato, and herbs.",
                "Whisk olive oil with lemon juice and oregano.",
                "Toss everything and finish with feta."
            ],
            ingredients: ["Quinoa", "Cucumber", "Tomato", "Feta", "Olive oil", "Lemon", "Oregano", "Red onion"],
            isQuick: false
        ),
    ]
}
