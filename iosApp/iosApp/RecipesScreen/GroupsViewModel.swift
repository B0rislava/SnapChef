//
//  GroupsViewModel.swift
//  iosApp
//
//  Created by gergana on 3/27/26.
//
import Foundation
import Combine

@MainActor
final class GroupsViewModel: ObservableObject {
    @Published private(set) var groups: [RecipeGroup] = []
    @Published private(set) var selectedGroupId: String = "personal"
    @Published var dialogMode: GroupDialogMode? = nil
    @Published var joinCodeInput: String = ""
    @Published var createNameInput: String = ""
    @Published private(set) var selectedRecipe: SharedRecipe? = nil
    @Published private(set) var infoMessage: String? = nil

    var selectedGroup: RecipeGroup? {
        groups.first { $0.id == selectedGroupId } ?? groups.first
    }

    private var cancellables = Set<AnyCancellable>()
    private let store = RecipeStore.shared

    init() {
        groups = defaultGroups()

        store.$personalRecipes
            .sink { [weak self] saved in
                guard let self else { return }
                self.groups = self.groups.map { group in
                    guard group.id == "personal" else { return group }
                    var updated = group
                    updated.recipes = self.personalBaseRecipes() + saved
                    return updated
                }
            }
            .store(in: &cancellables)

        store.$sharedRecipes
            .sink { [weak self] shared in
                guard let self else { return }
                self.groups = self.groups.map { group in
                    guard group.id == "g1" else { return group }
                    var updated = group
                    updated.recipes = self.flatmatesBaseRecipes() + shared
                    return updated
                }
            }
            .store(in: &cancellables)
    }

    // Actions

    func selectGroup(_ id: String) {
        selectedGroupId = id
    }

    func openDialog(_ mode: GroupDialogMode) {
        dialogMode = mode
    }

    func closeDialog() {
        dialogMode = nil
    }

    func openRecipe(_ recipe: SharedRecipe) {
        selectedRecipe = recipe
    }

    func closeRecipeDetails() {
        selectedRecipe = nil
    }

    func joinGroup() {
        let code = joinCodeInput.trimmingCharacters(in: .whitespaces).uppercased()
        guard code.count >= 4 else {
            infoMessage = "Please enter a valid group code."
            dialogMode = nil
            joinCodeInput = ""
            return
        }

        if let existing = groups.first(where: { $0.code == code }) {
            selectedGroupId = existing.id
            infoMessage = "You are already in this group."
            dialogMode = nil
            joinCodeInput = ""
            return
        }

        let joined = RecipeGroup(
            id: "joined_\(Int.random(in: 1000...9999))",
            name: "Group \(String(code.prefix(4)))",
            code: code,
            recipes: [
                SharedRecipe(
                    title: "Shared Soup",
                    description: "Group shared soup recipe.",
                    ownerName: "Anton",
                    missingItems: ["2 eggs"],
                    instructions: [
                        "Boil water in a medium pot.",
                        "Add vegetables and simmer for 10 minutes.",
                        "Season and serve warm.",
                    ]
                )
            ]
        )
        groups.append(joined)
        selectedGroupId = joined.id
        infoMessage = "Joined group \(joined.name)."
        dialogMode = nil
        joinCodeInput = ""
    }

    func createGroup() {
        let name = createNameInput.trimmingCharacters(in: .whitespaces)
        guard !name.isEmpty else {
            infoMessage = "Group name cannot be empty."
            dialogMode = nil
            createNameInput = ""
            return
        }

        let code = Self.generateGroupCode()
        let created = RecipeGroup(
            id: "created_\(Int.random(in: 1000...9999))",
            name: name,
            code: code,
            recipes: []
        )
        groups.append(created)
        selectedGroupId = created.id
        infoMessage = "Group created. Code: \(code)"
        dialogMode = nil
        createNameInput = ""
    }

    // Default Data

    private func defaultGroups() -> [RecipeGroup] {
        [
            RecipeGroup(
                id: "personal",
                name: "Your recipes",
                code: nil,
                recipes: personalBaseRecipes(),
                isPersonal: true
            ),
            RecipeGroup(
                id: "g1",
                name: "Flatmates",
                code: "A7K2P1",
                recipes: flatmatesBaseRecipes()
            ),
        ]
    }

    private func personalBaseRecipes() -> [SharedRecipe] {
        [
            SharedRecipe(
                title: "Tomato Omelette",
                description: "Soft omelette with tomatoes and herbs.",
                ownerName: "You",
                missingItems: [],
                availableItems: ["Eggs", "Tomatoes", "Salt", "Pepper", "Oil", "Fresh Herbs"],
                instructions: [
                    "Whisk eggs with salt and pepper.",
                    "Cook tomatoes for 2 minutes.",
                    "Pour eggs and cook until set.",
                ]
            ),
            SharedRecipe(
                title: "Chicken Rice Bowl",
                description: "Rice bowl with chicken and green vegetables.",
                ownerName: "You",
                missingItems: [],
                availableItems: ["Chicken breast", "Rice", "Green onion", "Soy sauce", "Sesame seeds"],
                instructions: [
                    "Cook rice and keep warm.",
                    "Saute chicken until fully cooked.",
                    "Add vegetables and stir for 3 minutes.",
                    "Serve over rice.",
                ],
                perishableProducts: [
                    PerishableProduct(name: "Chicken breast", maxFreshDays: 2, freshness: 0.65),
                    PerishableProduct(name: "Green onion",    maxFreshDays: 4, freshness: 0.45),
                ]
            ),
        ]
    }

    private func flatmatesBaseRecipes() -> [SharedRecipe] {
        [
            SharedRecipe(
                title: "Pasta Carbonara",
                description: "Classic creamy pasta with bacon and parmesan.",
                ownerName: "Anton",
                missingItems: ["2 eggs"],
                instructions: [
                    "Cook pasta in salted water.",
                    "Fry bacon until crisp.",
                    "Mix eggs and cheese in a bowl.",
                    "Combine pasta with bacon and egg mix off the heat.",
                ],
                perishableProducts: [
                    PerishableProduct(name: "Bacon", maxFreshDays: 3, freshness: 0.18),
                    PerishableProduct(name: "Eggs",  maxFreshDays: 6, freshness: 0.42),
                ]
            ),
            SharedRecipe(
                title: "Veggie Stir Fry",
                description: "Fast stir fry with peppers and soy sauce.",
                ownerName: "Mira",
                missingItems: ["1 red pepper"],
                instructions: [
                    "Chop all vegetables evenly.",
                    "Heat wok and add oil.",
                    "Stir-fry vegetables for 4-5 minutes.",
                    "Add soy sauce and serve.",
                ],
                perishableProducts: [
                    PerishableProduct(name: "Bell pepper", maxFreshDays: 5, freshness: 0.06),
                    PerishableProduct(name: "Mushrooms",   maxFreshDays: 2, freshness: 0.0),
                ]
            ),
        ]
    }

    private static func generateGroupCode() -> String {
        let chars = Array("ABCDEFGHJKLMNPQRSTUVWXYZ23456789")
        return String((0..<6).map { _ in chars.randomElement()! })
    }
}
