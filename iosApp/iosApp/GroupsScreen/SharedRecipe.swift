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
    let id: String
    var title: String
    var description: String
    var ownerName: String
    var missingItems: [String]
    var availableItems: [String] = []
    var instructions: [String] = []
    var perishableProducts: [PerishableProduct] = []
    var serverSharedRecipeId: Int?
    var sessionRecipeId: Int?
    var catalogRecipeId: Int?
    var isCatalogStarred: Bool?
    var isSessionFavorited: Bool?

    init(
        id: String = UUID().uuidString,
        title: String,
        description: String,
        ownerName: String,
        missingItems: [String],
        availableItems: [String] = [],
        instructions: [String] = [],
        perishableProducts: [PerishableProduct] = [],
        serverSharedRecipeId: Int? = nil,
        sessionRecipeId: Int? = nil,
        catalogRecipeId: Int? = nil,
        isCatalogStarred: Bool? = nil,
        isSessionFavorited: Bool? = nil
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
        self.sessionRecipeId = sessionRecipeId
        self.catalogRecipeId = catalogRecipeId
        self.isCatalogStarred = isCatalogStarred
        self.isSessionFavorited = isSessionFavorited
    }

    func ingredientsForShareRequest() -> [String] {
        func strip(_ s: String) -> String {
            s.replacingOccurrences(of: " (from you)", with: "")
                .replacingOccurrences(of: " (from group)", with: "")
        }
        var seen = Set<String>()
        var out: [String] = []
        for x in (availableItems.map(strip) + missingItems) {
            let t = x.trimmingCharacters(in: .whitespacesAndNewlines)
            if t.isEmpty { continue }
            if !seen.insert(t).inserted { continue }
            out.append(t)
        }
        if out.isEmpty, !title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            return [title]
        }
        return out
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
