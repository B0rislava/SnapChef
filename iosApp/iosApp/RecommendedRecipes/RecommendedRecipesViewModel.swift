import Foundation
import Shared

struct RecommendedRecipeItem: Identifiable, Equatable {
    let id: Int
    var title: String
    var description: String
    var instructions: [String]
    var ingredients: [String]
    var isQuick: Bool
    var minutes: Int?
    var uses: [String]
    var extra: [String]
    var shareSessionRecipeId: Int? = nil
    var shareCatalogRecipeId: Int? = nil

    static func fromLibrary(_ r: LibraryRecipeOut) -> RecommendedRecipeItem {
        let u = toStringsFromAny(r.uses)
        let e = toStringsFromAny(r.extra)
        let steps = toStringsFromAny(r.steps)
        let instr = toStringsFromAny(r.instructions)
        let ings = toStringsFromAny(r.ingredients)
        let stepList: [String]
        if !steps.isEmpty {
            stepList = steps
        } else if !instr.isEmpty {
            stepList = instr
        } else {
            stepList = []
        }
        let pantryLine: [String] = (u.isEmpty && e.isEmpty) ? ings : (u + e)
        let mInt: Int? = {
            let o = (r as AnyObject).value(forKey: "minutes")
            if o is NSNull || o == nil { return nil }
            if let n = o as? Int { return n }
            if let n = o as? Int32 { return Int(n) }
            if let n = o as? NSNumber { return n.intValue }
            return (o as? KotlinInt).map { Int($0.intValue) }
        }()
        let mFromCook: Int? = {
            let o = (r as AnyObject).value(forKey: "cookTimeMinutes")
            if o is NSNull || o == nil { return nil }
            if let n = o as? Int { return n }
            if let n = o as? Int32 { return Int(n) }
            if let n = o as? NSNumber { return n.intValue }
            return (o as? KotlinInt).map { Int($0.intValue) }
        }()
        let minutes: Int? = mInt ?? mFromCook
        let isQuick: Bool
        if let m = minutes {
            isQuick = m < 30
        } else {
            isQuick = stepList.count < 4
        }
        let name = (r as AnyObject).value(forKey: "name") as? String
        let titleField = (r as AnyObject).value(forKey: "title") as? String
        let titleText: String
        if let n = name, !n.isEmpty { titleText = n }
        else if let t = titleField, !t.isEmpty { titleText = t }
        else { titleText = "Recipe" }
        var rid = intFromKotlin(r.id)
        if rid == 0 {
            rid = abs(titleText.hashValue) % 2_000_000_000
            if rid == 0 { rid = 1 }
        }
        let blurb: String
        if let m = minutes {
            blurb = "\(m) min"
        } else {
            blurb = ""
        }
        let body: String? = (r as AnyObject).value(forKey: "description_") as? String
        let desc: String
        if let b = body, !b.isEmpty {
            desc = b
        } else if !blurb.isEmpty {
            desc = "\(blurb) · picks for you"
        } else {
            desc = "Recommended for you"
        }
        return RecommendedRecipeItem(
            id: rid,
            title: titleText,
            description: desc,
            instructions: stepList,
            ingredients: pantryLine,
            isQuick: isQuick,
            minutes: minutes,
            uses: u,
            extra: e,
            shareCatalogRecipeId: rid > 0 ? rid : nil
        )
    }

    static func from(_ r: SessionRecipeOut) -> RecommendedRecipeItem {
        let u = toStringsFromAny(r.uses)
        let e = toStringsFromAny(r.extra)
        let steps = toStringsFromAny(r.steps)
        let nameOpt = (r as AnyObject).value(forKey: "name") as? String
        let titleOpt = (r as AnyObject).value(forKey: "title") as? String
        let resolvedTitle: String
        if let n = nameOpt?.trimmingCharacters(in: .whitespacesAndNewlines), !n.isEmpty { resolvedTitle = n }
        else if let t = titleOpt?.trimmingCharacters(in: .whitespacesAndNewlines), !t.isEmpty { resolvedTitle = t }
        else { resolvedTitle = "Recipe" }
        let all = u + e
        let mInt: Int? = {
            let o = (r as AnyObject).value(forKey: "minutes")
            if o == nil || o is NSNull { return nil }
            if let n = o as? Int { return n }
            if let n = o as? Int32 { return Int(n) }
            if let n = o as? NSNumber { return n.intValue }
            return (o as? KotlinInt).map { Int($0.intValue) }
        }()
        let isQuick: Bool
        if let m = mInt {
            isQuick = m < 30
        } else {
            isQuick = steps.count < 4
        }
        let mLabel: String
        if let m = mInt {
            mLabel = "\(m) min"
        } else {
            mLabel = ""
        }
        let desc: String
        if mLabel.isEmpty {
            desc = "AI-suggested. You have \(u.count) in pantry; \(e.count) to buy."
        } else {
            desc = "\(mLabel) · you have \(u.count); shop \(e.count) more."
        }
        let sid = intFromKotlin(r.id)
        return RecommendedRecipeItem(
            id: sid,
            title: resolvedTitle,
            description: desc,
            instructions: steps,
            ingredients: all,
            isQuick: isQuick,
            minutes: mInt,
            uses: u,
            extra: e,
            shareSessionRecipeId: sid
        )
    }

    func toSharedRecipe() -> SharedRecipe {
        SharedRecipe(
            title: title,
            description: description,
            ownerName: "You",
            missingItems: extra,
            availableItems: uses.map { "\($0) (from you)" },
            instructions: instructions,
            perishableProducts: [],
            sessionRecipeId: shareSessionRecipeId,
            catalogRecipeId: shareCatalogRecipeId
        )
    }

    func toFavoriteRecord() -> FavoriteRecipeRecord {
        FavoriteRecipeRecord(
            id: id,
            title: title,
            description: description,
            instructions: instructions,
            ingredients: ingredients,
            uses: uses,
            extra: extra,
            isQuick: isQuick,
            minutes: minutes
        )
    }
}

extension FavoriteRecipeRecord {
    func toItem() -> RecommendedRecipeItem {
        RecommendedRecipeItem(
            id: id,
            title: title,
            description: description,
            instructions: instructions,
            ingredients: ingredients,
            isQuick: isQuick,
            minutes: minutes,
            uses: uses,
            extra: extra
        )
    }
}

private func intFromKotlin(_ v: Any) -> Int {
    if let n = v as? NSNumber { return n.intValue }
    if let k = v as? KotlinInt { return Int(k.intValue) }
    if let i = v as? Int { return i }
    if let i32 = v as? Int32 { return Int(i32) }
    return 0
}

private func toStringsFromAny(_ v: Any?) -> [String] {
    guard let v = v else { return [] }
    if let a = v as? [String] { return a }
    if let a = v as? NSArray { return a.compactMap { $0 as? String } }
    return []
}

struct RecommendedRecipesUiState {
    var recipes:            [RecommendedRecipeItem] = []
    var openedRecipe:       RecommendedRecipeItem?  = nil
    var checkedIngredients:   [String: Bool]         = [:]
    var infoMessage:        String?                 = nil
    var isLoading:          Bool                    = false
    var errorMessage:       String?                 = nil
}

@MainActor
final class RecommendedRecipesViewModel: ObservableObject {
    @Published var uiState = RecommendedRecipesUiState()
    private let homeService = SnapChefServiceLocator.shared.homeApiService
    private var loadGeneration: Int = 0

    func loadRecommendations(flow: AppFlowState) {
        loadGeneration &+= 1
        let gen = loadGeneration
        Task { await self.performLoad(flow: flow, generation: gen) }
    }

    func refreshRecommendations(flow: AppFlowState) async {
        loadGeneration &+= 1
        let gen = loadGeneration
        await performLoad(flow: flow, generation: gen)
    }

    private func mapRecipesFromResponse(_ response: GroqRecipeSuggestResponse) -> [RecommendedRecipeItem] {
        response.recipes.map { RecommendedRecipeItem.from($0) }
    }

    private func mapLibraryList(_ res: RecommendedTabResponse) -> [RecommendedRecipeItem] {
        res.recipes.map { RecommendedRecipeItem.fromLibrary($0) }
    }

    private func mergeSessionFirst(session: [RecommendedRecipeItem], library: [RecommendedRecipeItem]) -> [RecommendedRecipeItem] {
        var seen = Set<Int>()
        var out: [RecommendedRecipeItem] = []
        for r in session + library {
            if seen.insert(r.id).inserted { out.append(r) }
        }
        return out
    }

    private func performLoad(flow: AppFlowState, generation: Int) async {
        if generation != loadGeneration { return }
        await MainActor.run {
            if generation == loadGeneration {
                uiState.isLoading = true
                uiState.errorMessage = nil
                uiState.infoMessage = nil
            }
        }
        if generation != loadGeneration { return }
        var tabError: String?
        var libraryItems: [RecommendedRecipeItem] = []
        do {
            let res = try await homeService.fetchRecommendedTabRecipes(count: 8)
            if generation != loadGeneration { return }
            libraryItems = mapLibraryList(res)
        } catch {
            tabError = error.localizedDescription
        }
        var sessionItems: [RecommendedRecipeItem] = []
        if let sid = flow.lastSessionId {
            do {
                let response = try await homeService.suggestRecipes(sessionId: sid)
                if generation != loadGeneration { return }
                sessionItems = mapRecipesFromResponse(response)
            } catch { }
        }
        let merged = mergeSessionFirst(session: sessionItems, library: libraryItems)
        await MainActor.run {
            guard generation == loadGeneration else { return }
            uiState.recipes = merged
            if merged.isEmpty {
                if let te = tabError, sessionItems.isEmpty {
                    uiState.errorMessage = "Could not load recommendations. \(te)"
                } else if tabError != nil, !sessionItems.isEmpty {
                    uiState.errorMessage = nil
                } else {
                    uiState.errorMessage = "No recommendations right now. Pull to refresh or add a Home scan for tailored ideas."
                }
            } else {
                uiState.errorMessage = nil
            }
            uiState.isLoading = false
        }
    }

    func openRecipe(_ recipe: RecommendedRecipeItem) {
        uiState.openedRecipe = recipe
        uiState.checkedIngredients = Dictionary(
            uniqueKeysWithValues: recipe.ingredients.map { ($0, true) }
        )
        uiState.infoMessage = nil
    }

    func openRecipeIndex(_ index: Int) {
        guard index < uiState.recipes.count else { return }
        openRecipe(uiState.recipes[index])
    }

    func openFavorite(_ record: FavoriteRecipeRecord) {
        openRecipe(record.toItem())
    }

    func closeRecipe() {
        uiState.openedRecipe = nil
        uiState.checkedIngredients = [:]
        uiState.infoMessage = nil
    }

    func toggleIngredient(_ ingredient: String, checked: Bool) {
        uiState.checkedIngredients[ingredient] = checked
    }

    func setInfoMessage(_ value: String?) {
        uiState.infoMessage = value
    }

    func saveCurrentRecipe() {
        guard let r = uiState.openedRecipe else { return }
        let m = r.toSharedRecipe()
        let saved = SharedRecipe(
            id: m.id,
            title: m.title,
            description: m.description,
            ownerName: m.ownerName,
            missingItems: m.missingItems,
            availableItems: m.availableItems,
            instructions: m.instructions,
            perishableProducts: m.perishableProducts,
            serverSharedRecipeId: m.serverSharedRecipeId,
            sessionRecipeId: m.sessionRecipeId,
            catalogRecipeId: m.catalogRecipeId,
            isCatalogStarred: false,
            isSessionFavorited: m.isSessionFavorited
        )
        RecipeStore.shared.addPersonalRecipe(saved)
        SavedRecipesCloudSync.run()
        uiState.infoMessage = NSLocalizedString("saved", comment: "")
    }

    func toggleFavoriteCurrent() {
        guard let r = uiState.openedRecipe else { return }
        FavoritesStore.shared.toggle(r.toFavoriteRecord())
    }
}
