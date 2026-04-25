import Foundation
import Shared

@MainActor
final class RecipeStore: ObservableObject {
    static let shared = RecipeStore()

    @Published private(set) var personalRecipes: [SharedRecipe] = []
    @Published private(set) var favoriteRecipeKeys: Set<String> = []

    private static let keySavedPrefix = "saved_recipes_user_"
    private static let keyFavoritesPrefix = "favorite_keys_user_"
    private static let legacySaved = "saved_recipes"
    private static let legacyFav = "favorite_keys"

    private let defaults: UserDefaults

    private init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
        reloadFromStorageForCurrentUser()
    }

    private func currentUserId() -> Int {
        let id = AuthManager.shared.currentUser?.id
        if let k = id as? KotlinInt { return Int(k.intValue) }
        if let n = id as? NSNumber { return n.intValue }
        return 0
    }

    private func keySaved() -> String { "\(Self.keySavedPrefix)\(currentUserId())" }
    private func keyFavorites() -> String { "\(Self.keyFavoritesPrefix)\(currentUserId())" }

    func reloadFromStorageForCurrentUser() {
        migrateLegacyIfNeeded()
        personalRecipes = loadSavedRecipes()
        favoriteRecipeKeys = loadFavoriteKeys()
    }

    private func migrateLegacyIfNeeded() {
        let sk = keySaved()
        let fk = keyFavorites()
        if defaults.string(forKey: sk) == nil, let l = defaults.string(forKey: Self.legacySaved), !l.isEmpty {
            defaults.set(l, forKey: sk)
        }
        if defaults.string(forKey: fk) == nil, let l = defaults.string(forKey: Self.legacyFav), !l.isEmpty {
            defaults.set(l, forKey: fk)
        }
    }

    func addPersonalRecipe(_ recipe: SharedRecipe) {
        var next = [recipe] + personalRecipes
        var seen = Set<String>()
        next = next.filter { seen.insert($0.favoriteKey()).inserted }
        personalRecipes = next
        persistSaved(next)
    }

    func removePersonalRecipe(_ recipe: SharedRecipe) {
        let k = recipe.favoriteKey()
        let next = personalRecipes.filter { $0.favoriteKey() != k }
        personalRecipes = next
        persistSaved(next)
        removeFavoriteLocal(recipe)
    }

    func toggleFavoriteLocal(_ recipe: SharedRecipe) {
        let k = recipe.favoriteKey()
        if favoriteRecipeKeys.contains(k) {
            favoriteRecipeKeys = favoriteRecipeKeys.subtracting([k])
        } else {
            favoriteRecipeKeys = favoriteRecipeKeys.union([k])
        }
        persistFavorites(favoriteRecipeKeys)
    }

    func removeFavoriteLocal(_ recipe: SharedRecipe) {
        let k = recipe.favoriteKey()
        favoriteRecipeKeys = favoriteRecipeKeys.subtracting([k])
        persistFavorites(favoriteRecipeKeys)
    }

    func isFavoriteLocal(_ recipe: SharedRecipe) -> Bool {
        favoriteRecipeKeys.contains(recipe.favoriteKey())
    }

    private func loadFavoriteKeys() -> Set<String> {
        if let d = defaults.data(forKey: keyFavorites()),
           let arr = try? JSONDecoder().decode([String].self, from: d) {
            return Set(arr)
        }
        return []
    }

    private func persistFavorites(_ keys: Set<String>) {
        if let d = try? JSONEncoder().encode(Array(keys)) {
            defaults.set(d, forKey: keyFavorites())
        }
    }

    private func loadSavedRecipes() -> [SharedRecipe] {
        let d: Data?
        if let data = defaults.data(forKey: keySaved()) { d = data }
        else if let raw = defaults.string(forKey: keySaved()) { d = raw.data(using: .utf8) }
        else { return [] }
        guard let data = d, let j = try? JSONSerialization.jsonObject(with: data) as? [[String: Any]] else { return [] }
        return j.compactMap { decodeRow($0) }
    }

    private func decodeRow(_ obj: [String: Any]) -> SharedRecipe? {
        let title = (obj["title"] as? String) ?? ""
        let desc = (obj["description"] as? String) ?? ""
        let owner = (obj["ownerName"] as? String) ?? "You"
        let missing = (obj["missingItems"] as? [String]) ?? []
        let avail = (obj["availableItems"] as? [String]) ?? []
        let steps = (obj["instructions"] as? [String]) ?? []
        let sid = intOpt(obj["sessionRecipeId"])
        let cid = intOpt(obj["catalogRecipeId"])
        var bid = intOpt(obj["backendSharedId"])
        if bid == nil { bid = intOpt(obj["serverSharedRecipeId"]) }
        let isStar: Bool? = {
            if let b = obj["isCatalogStarred"] as? Bool { return b }
            return nil
        }()
        let recipe = SharedRecipe(
            id: stableLocalId(fallbackTitle: title, steps: steps, sid: sid, cid: cid, bid: bid),
            title: title,
            description: desc,
            ownerName: owner,
            missingItems: missing,
            availableItems: avail,
            instructions: steps,
            perishableProducts: [],
            serverSharedRecipeId: bid,
            sessionRecipeId: sid,
            catalogRecipeId: cid,
            isCatalogStarred: isStar
        )
        return recipe
    }

    private func intOpt(_ any: Any?) -> Int? {
        if any == nil || any is NSNull { return nil }
        if let i = any as? Int { return i }
        if let i = any as? Int32 { return Int(i) }
        if let n = any as? NSNumber { return n.intValue }
        return nil
    }

    private func stableLocalId(
        fallbackTitle: String,
        steps: [String],
        sid: Int?,
        cid: Int?,
        bid: Int?
    ) -> String {
        if let b = bid { return "srv-\(b)" }
        if let c = cid { return "cat-\(c)" }
        if let s = sid { return "sess-\(s)" }
        let t = fallbackTitle.lowercased()
        let h = (t + steps.joined()).hashValue
        return "local-\(h)"
    }

    private func persistSaved(_ recipes: [SharedRecipe]) {
        var arr: [[String: Any]] = []
        for r in recipes {
            var o: [String: Any] = [
                "title": r.title,
                "description": r.description,
                "ownerName": r.ownerName,
                "missingItems": r.missingItems,
                "availableItems": r.availableItems,
                "instructions": r.instructions,
            ]
            if let v = r.sessionRecipeId { o["sessionRecipeId"] = v }
            if let v = r.catalogRecipeId { o["catalogRecipeId"] = v }
            if let v = r.serverSharedRecipeId { o["backendSharedId"] = v }
            if let v = r.isCatalogStarred { o["isCatalogStarred"] = v }
            arr.append(o)
        }
        if let d = try? JSONSerialization.data(withJSONObject: arr, options: []),
           let s = String(data: d, encoding: .utf8) {
            defaults.set(s, forKey: keySaved())
        }
    }
}
