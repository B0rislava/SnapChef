//
//  ProfileViewModel.swift
//  iosApp
//
//  Created by gergana on 3/27/26.
//

import Foundation
import Combine

struct ProfileSavedRecipe: Identifiable {
    let id = UUID()
    let title: String
    let isQuick: Bool
}

struct ProfileUiState {
    var recipes: [ProfileSavedRecipe] = [
        ProfileSavedRecipe(title: "Omelette with Cheese", isQuick: true),
        ProfileSavedRecipe(title: "Tomato Egg Fried Rice", isQuick: true),
        ProfileSavedRecipe(title: "Baked Veggie Pasta", isQuick: false),
        ProfileSavedRecipe(title: "Leftover Chicken Wraps", isQuick: true)
    ]
}

@MainActor
class ProfileViewModel: ObservableObject {
    @Published var uiState = ProfileUiState()
    
    func applyBackendJson(json: String) {
        guard let data = json.data(using: .utf8),
              let dict = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let array = dict["recipes"] as? [[String: Any]] else {
            return
        }
        
        let mapped = array.compactMap { item -> ProfileSavedRecipe? in
            let title = item["title"] as? String ?? "Untitled recipe"
            let isQuick = item["isQuick"] as? Bool ?? false
            return ProfileSavedRecipe(title: title, isQuick: isQuick)
        }
        
        if !mapped.isEmpty {
            uiState.recipes = mapped
        }
    }
}
