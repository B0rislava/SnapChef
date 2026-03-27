//
//  RecipeGroup.swift
//  iosApp
//
//  Created by gergana on 3/27/26.
//

import Foundation

struct RecipeGroup: Identifiable, Equatable {
    let id: String
    let name: String
    let code: String?
    var recipes: [SharedRecipe]
    var isPersonal: Bool = false
}
