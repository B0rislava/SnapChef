import Foundation

struct FavoriteRecipeRecord: Identifiable, Codable, Equatable {
    var id: Int
    var title: String
    var description: String
    var instructions: [String]
    var ingredients: [String]
    var uses: [String]
    var extra: [String]
    var isQuick: Bool
    var minutes: Int?
}

@MainActor
final class FavoritesStore: ObservableObject {
    static let shared = FavoritesStore()

    @Published private(set) var items: [FavoriteRecipeRecord] = []

    private let key = "SnapChef.favoriteRecipeRecords.v2"

    private init() { load() }

    func isFavorite(id: Int) -> Bool {
        items.contains { $0.id == id }
    }

    func toggle(_ record: FavoriteRecipeRecord) {
        if let i = items.firstIndex(where: { $0.id == record.id }) {
            items.remove(at: i)
        } else {
            items.insert(record, at: 0)
        }
        save()
    }

    private func load() {
        guard let d = UserDefaults.standard.data(forKey: key),
              let decoded = try? JSONDecoder().decode([FavoriteRecipeRecord].self, from: d) else { return }
        items = decoded
    }

    private func save() {
        if let data = try? JSONEncoder().encode(items) {
            UserDefaults.standard.set(data, forKey: key)
        }
    }
}
