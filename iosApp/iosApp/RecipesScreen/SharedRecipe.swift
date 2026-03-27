//
//  SharedRecipe.swift
//  iosApp
//
//  Created by gergana on 3/27/26.
//
import Foundation

struct PerishableProduct {
    let name: String
    let maxFreshDays: Int
    let freshness: Float

    func daysLeft() -> Int {
        return Int(ceil(Double(maxFreshDays + 1) * Double(freshness))) - 1
    }
}

struct SharedRecipe: Identifiable, Equatable {
    let id = UUID()
    let title: String
    let description: String
    let ownerName: String
    let missingItems: [String]
    var availableItems: [String] = []
    var instructions: [String] = []
    var perishableProducts: [PerishableProduct] = []

    static func == (lhs: SharedRecipe, rhs: SharedRecipe) -> Bool {
        lhs.id == rhs.id
    }
}

extension SharedRecipe {
    func earliestDaysLeft() -> Int? {
        perishableProducts.map { $0.daysLeft() }.min()
    }

    func isExpired() -> Bool {
        (earliestDaysLeft() ?? Int.max) < 0
    }

    func expiresToday() -> Bool {
        (earliestDaysLeft() ?? Int.max) == 0
    }

    func spoiledProducts() -> [String] {
        perishableProducts.filter { $0.daysLeft() < 0 }.map { $0.name }
    }
}
