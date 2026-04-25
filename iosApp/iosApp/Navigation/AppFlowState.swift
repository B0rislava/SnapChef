import Foundation
import SwiftUI

@MainActor
final class AppFlowState: ObservableObject {
    @Published var lastSessionId: Int32?
    @Published var lastIngredients: [String] = []
    @Published var recipeSessionVersion: Int = 0

    private let defaults: UserDefaults
    private let sessionIdKey = "appFlow.lastSessionId"
    private let ingredientsKey = "appFlow.lastIngredients"
    private let recipeVersionKey = "appFlow.recipeSessionVersion"

    init(userDefaults: UserDefaults = .standard) {
        self.defaults = userDefaults
        if defaults.object(forKey: sessionIdKey) != nil {
            let v = defaults.integer(forKey: sessionIdKey)
            if v > 0 { lastSessionId = Int32(v) }
        }
        if let data = defaults.data(forKey: ingredientsKey),
           let ings = try? JSONDecoder().decode([String].self, from: data) {
            lastIngredients = ings
        }
        let ver = defaults.integer(forKey: recipeVersionKey)
        if ver > 0 { recipeSessionVersion = ver }
    }

    func notifyRecipeSession(id: Int32, ingredients: [String]) {
        lastSessionId = id
        lastIngredients = ingredients
        recipeSessionVersion &+= 1
        persist()
    }

    private func persist() {
        if let sid = lastSessionId {
            defaults.set(Int(sid), forKey: sessionIdKey)
        } else {
            defaults.removeObject(forKey: sessionIdKey)
        }
        if let data = try? JSONEncoder().encode(lastIngredients) {
            defaults.set(data, forKey: ingredientsKey)
        }
        defaults.set(recipeSessionVersion, forKey: recipeVersionKey)
    }
}
