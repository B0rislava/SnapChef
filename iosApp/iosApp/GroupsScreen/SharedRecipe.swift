import Foundation

struct PerishableProduct: Identifiable, Equatable {
    let id = UUID()
    let name: String
    let maxFreshDays: Int
    let freshness: Float
    
    func daysLeft() -> Int {
        return Int(ceil(Float(maxFreshDays + 1) * freshness)) - 1
    }
}

struct SharedRecipe: Identifiable, Equatable {
    let id = UUID()
    var title: String
    var description: String
    var ownerName: String
    var missingItems: [String]
    var availableItems: [String] = []
    var instructions: [String] = []
    var perishableProducts: [PerishableProduct] = []
    
    func earliestDaysLeft() -> Int? {
        return perishableProducts.map { $0.daysLeft() }.min()
    }
    
    func isExpired() -> Bool {
        return (earliestDaysLeft() ?? Int.max) < 0
    }
    
    func expiresToday() -> Bool {
        return (earliestDaysLeft() ?? Int.max) == 0
    }
    
    func spoiledProducts() -> [String] {
        return perishableProducts.filter { $0.daysLeft() < 0 }.map { $0.name }
    }
}
