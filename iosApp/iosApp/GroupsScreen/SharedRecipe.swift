import Foundation

struct PerishableProduct: Identifiable, Equatable {
    let id: UUID
    let name: String
    let maxFreshDays: Int
    let freshness: Float

    init(id: UUID = UUID(), name: String, maxFreshDays: Int, freshness: Float) {
        self.id = id
        self.name = name
        self.maxFreshDays = maxFreshDays
        self.freshness = freshness
    }

    func daysLeft() -> Int {
        return Int(ceil(Float(maxFreshDays + 1) * freshness)) - 1
    }
}

struct SharedRecipe: Identifiable, Equatable {
    /// Local client id, or `srv-<server id>` for recipes loaded from the API.
    let id: String
    var title: String
    var description: String
    var ownerName: String
    var missingItems: [String]
    var availableItems: [String] = []
    var instructions: [String] = []
    var perishableProducts: [PerishableProduct] = []
    /// `POST /share/recipe` record id, when this row was loaded from `GET /share/group/{id}/recipes`
    var serverSharedRecipeId: Int?

    init(
        id: String = UUID().uuidString,
        title: String,
        description: String,
        ownerName: String,
        missingItems: [String],
        availableItems: [String] = [],
        instructions: [String] = [],
        perishableProducts: [PerishableProduct] = [],
        serverSharedRecipeId: Int? = nil
    ) {
        self.id = id
        self.title = title
        self.description = description
        self.ownerName = ownerName
        self.missingItems = missingItems
        self.availableItems = availableItems
        self.instructions = instructions
        self.perishableProducts = perishableProducts
        self.serverSharedRecipeId = serverSharedRecipeId
    }

    func allIngredientPhrasesForShare() -> [String] {
        let have = availableItems.map { $0.replacingOccurrences(of: " (from you)", with: "") }
        return have + missingItems
    }

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
