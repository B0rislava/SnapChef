import Foundation
import Shared
import Combine

struct GroupMember: Identifiable {
    let id = UUID()
    let realId: Int32
    let name: String
    let avatarSeed: String
}

struct AppGroup: Identifiable {
    let id: String
    var name: String
    var code: String?
    var ownerName: String?
    var members: [GroupMember]
    var isAdmin: Bool
    var recipes: [SharedRecipe] = []
    var isPersonal: Bool = false
}

enum GroupDialogMode: Identifiable {
    case choice, join, create
    var id: Int {
        switch self { case .choice: return 0; case .join: return 1; case .create: return 2 }
    }
}

@MainActor
final class GroupsViewModel: ObservableObject {

    @Published private(set) var groups: [AppGroup] = []
    @Published var selectedId: String? = nil
    @Published var selectedRecipe: SharedRecipe?    = nil
    @Published var dialogMode: GroupDialogMode?  = nil
    @Published var joinCodeInput: String = ""
    @Published var createNameInput: String = ""
    @Published var isLoading: Bool = false
    @Published var infoMessage: String? = nil
    @Published var isError: Bool = false
    @Published var isDetailLoading: Bool = false

    private var cancellables = Set<AnyCancellable>()

    var selectedGroup: AppGroup? {
        guard let id = selectedId else { return groups.first }
        return groups.first { $0.id == id } ?? groups.first
    }
    
    var visibleGroups: [AppGroup] {
        return groups.filter { !$0.isPersonal }
    }
    
    var selectedSharedGroup: AppGroup? {
        let shared = visibleGroups
        guard let id = selectedId else { return shared.first }
        return shared.first { $0.id == id } ?? shared.first
    }

    private let apiService = SnapChefServiceLocator.shared.authApiService
    private let groupSharing = SnapChefServiceLocator.shared.groupSharingApiService
    private var infoTask: Task<Void, Never>?
    private var groupServerRecipes: [String: [SharedRecipe]] = [:]
    @Published var combinedPantryLabel: [String: String] = [:]
    @Published var isCombinedLoading: Bool = false

    init() {
        Task { await loadGroups() }
        RecipeStore.shared.$personalRecipes
            .receive(on: DispatchQueue.main)
            .sink { [weak self] recipes in
                guard let self = self else { return }
                if let idx = self.groups.firstIndex(where: { $0.isPersonal }) {
                    self.groups[idx].recipes = recipes
                }
            }
            .store(in: &cancellables)
    }

    func loadGroups() async {
        isLoading = true
        do {
            let raw = try await apiService.fetchGroups()
            let currentUserId = AuthManager.shared.currentUser?.id
            let remoteGroups = raw.map { g in
                let isAdmin = (g.createdByUserId == currentUserId)
                let idStr = String(g.id)
                let existing = self.groups.first(where: { $0.id == idStr })
                return AppGroup(
                    id: idStr,
                    name: g.name,
                    code: g.code,
                    ownerName: isAdmin ? "You" : (existing?.ownerName ?? nil),
                    members: existing?.members ?? [],
                    isAdmin: isAdmin,
                    recipes: self.groupServerRecipes[idStr] ?? [],
                    isPersonal: false
                )
            }
            
            let personalGroup = AppGroup(
                id: "personal",
                name: "Your recipes",
                code: nil,
                ownerName: "You",
                members: [GroupMember(realId: currentUserId ?? 0, name: "You", avatarSeed: "You")],
                isAdmin: false,
                recipes: RecipeStore.shared.personalRecipes,
                isPersonal: true
            )
            
            groups = [personalGroup] + remoteGroups
            
            if selectedId == nil {
                selectedId = "personal"
            }
            
            let idToLoad: String?
            if let id = selectedId, id != "personal" && !id.hasPrefix("group_") {
                idToLoad = id
            } else {
                idToLoad = remoteGroups.first?.id
            }
            
            if let id = idToLoad {
                await loadGroupDetail(id: id)
            }
            await refreshAllGroupRecipes(remoteGroups: remoteGroups)
        } catch {
            showInfo("Failed to load groups.", isError: true)
        }
        isLoading = false
    }

    private func refreshAllGroupRecipes(remoteGroups: [AppGroup]) async {
        for g in remoteGroups {
            let list = await fetchSharedRecipesList(groupId: g.id)
            groupServerRecipes[g.id] = list
            if let i = self.groups.firstIndex(where: { $0.id == g.id && !$0.isPersonal }) {
                self.groups[i].recipes = list
            }
        }
    }

    private func fetchSharedRecipesList(groupId: String) async -> [SharedRecipe] {
        guard let gid = Int32(groupId) else { return [] }
        do {
            let out = try await apiService.listGroupSharedRecipes(
                groupId: gid,
                limit: 50,
                offset: 0
            )
            return out.map { self.mapSharedRecipeOut($0) }
        } catch {
            return groupServerRecipes[groupId] ?? []
        }
    }

    private func mapSharedRecipeOut(_ o: SharedRecipeOut) -> SharedRecipe {
        let sid = intFromK((o as AnyObject).value(forKey: "id"))
        let authorId = intFromK((o as AnyObject).value(forKey: "userId"))
        let me = Int(AuthManager.shared.currentUser?.id ?? 0)
        let who = (authorId == me && me != 0) ? "You" : "Member"
        let title = (o as AnyObject).value(forKey: "title") as? String ?? "Recipe"
        var desc: String = ""
        if let d = (o as AnyObject).value(forKey: "description_") as? String, !d.isEmpty { desc = d }
        else if let d = (o as AnyObject).value(forKey: "description") as? String, !d.isEmpty { desc = d }
        let steps = toKotlinStringList((o as AnyObject).value(forKey: "steps")) ?? []
        let ings = toKotlinStringList((o as AnyObject).value(forKey: "ingredients")) ?? []
        let have = ings.map { "\($0) (from group)" }
        return SharedRecipe(
            id: "srv-\(sid)",
            title: title,
            description: desc,
            ownerName: who,
            missingItems: [],
            availableItems: have,
            instructions: steps,
            perishableProducts: [],
            serverSharedRecipeId: sid
        )
    }

    private func toKotlinStringList(_ v: Any?) -> [String]? {
        guard let v = v, !(v is NSNull) else { return [] }
        if let a = v as? [String] { return a }
        if let a = v as? NSArray { return a.compactMap { $0 as? String } }
        return nil
    }

    private func intFromK(_ v: Any?) -> Int {
        guard let v = v, !(v is NSNull) else { return 0 }
        if let n = v as? Int { return n }
        if let n = v as? Int32 { return Int(n) }
        if let n = v as? NSNumber { return n.intValue }
        if let n = v as? KotlinInt { return Int(n.intValue) }
        return 0
    }

    private func toKotlinIntOpt(_ n: Int?) -> KotlinInt? {
        guard let n = n else { return nil }
        return KotlinInt(value: Int32(n))
    }

    func shareRecipeToGroup(_ recipe: SharedRecipe, to group: AppGroup) {
        if group.isPersonal {
            showInfo("Pick a real group, not “Your recipes”.", isError: true)
            return
        }
        guard let gid = Int32(group.id) else { return }
        Task {
            isLoading = true
            do {
                let descTrim = recipe.description.trimmingCharacters(in: .whitespacesAndNewlines)
                let _ = try await apiService.shareRecipe(
                    request: ShareRecipeRequest(
                        groupId: gid,
                        title: recipe.title,
                        description: descTrim.isEmpty ? nil : descTrim,
                        ingredients: recipe.ingredientsForShareRequest(),
                        steps: recipe.instructions,
                        minutes: nil,
                        note: nil,
                        recipeId: toKotlinIntOpt(recipe.catalogRecipeId),
                        sessionRecipeId: toKotlinIntOpt(recipe.sessionRecipeId)
                    )
                )
                let list = await fetchSharedRecipesList(groupId: group.id)
                groupServerRecipes[group.id] = list
                if let i = self.groups.firstIndex(where: { $0.id == group.id }) {
                    self.groups[i].recipes = list
                }
                selectedId = group.id
                showInfo("Recipe shared to group.")
            } catch {
                showInfo("Could not share recipe. Try again. \(error.localizedDescription)", isError: true)
            }
            isLoading = false
        }
    }

    func deleteSharedRecipeFromGroup(_ recipe: SharedRecipe) {
        guard let g = selectedGroup, !g.isPersonal, let sid = recipe.serverSharedRecipeId else { return }
        Task {
            isLoading = true
            do {
                try await apiService.deleteSharedRecipe(sharedRecipeId: Int32(sid))
                let list = await fetchSharedRecipesList(groupId: g.id)
                groupServerRecipes[g.id] = list
                if let i = self.groups.firstIndex(where: { $0.id == g.id }) {
                    self.groups[i].recipes = list
                }
                if let sel = selectedRecipe, sel.id == recipe.id {
                    selectedRecipe = nil
                }
                showInfo("Recipe removed from group.")
            } catch {
                showInfo("Could not remove recipe. \(error.localizedDescription)", isError: true)
            }
            isLoading = false
        }
    }

    func toggleRecipeFavorite(_ recipe: SharedRecipe) {
        if let cid = recipe.catalogRecipeId {
            Task {
                isLoading = true
                do {
                    let fav = recipe.isCatalogStarred == true
                    if fav {
                        try await apiService.unstarCatalogRecipe(recipeId: Int32(cid))
                    } else {
                        try await apiService.starCatalogRecipe(recipeId: Int32(cid))
                    }
                    self.applyFavoritedState(recipeId: recipe.id, newCatalog: !fav, newSession: nil)
                } catch {
                    showInfo("Couldn’t update favorite. \(error.localizedDescription)", isError: true)
                }
                isLoading = false
            }
        } else if let sid = recipe.sessionRecipeId {
            Task {
                isLoading = true
                do {
                    let fav = recipe.isSessionFavorited == true
                    if fav {
                        _ = try await apiService.unfavoriteSessionRecipe(sessionRecipeId: Int32(sid))
                    } else {
                        _ = try await apiService.favoriteSessionRecipe(sessionRecipeId: Int32(sid))
                    }
                    self.applyFavoritedState(recipeId: recipe.id, newCatalog: nil, newSession: !fav)
                } catch {
                    showInfo("Couldn’t update favorite. \(error.localizedDescription)", isError: true)
                }
                isLoading = false
            }
        } else {
            showInfo("Favorites for this recipe are only available when it’s linked to your catalog or a scan session.", isError: true)
        }
    }

    private func applyFavoritedState(recipeId: String, newCatalog: Bool?, newSession: Bool?) {
        for i in 0..<groups.count {
            guard let j = groups[i].recipes.firstIndex(where: { $0.id == recipeId }) else { continue }
            var r = groups[i].recipes[j]
            if let c = newCatalog { r.isCatalogStarred = c }
            if let s = newSession { r.isSessionFavorited = s }
            groups[i].recipes[j] = r
        }
        for (gid, list) in groupServerRecipes {
            if let j = list.firstIndex(where: { $0.id == recipeId }) {
                var r = list[j]
                if let c = newCatalog { r.isCatalogStarred = c }
                if let s = newSession { r.isSessionFavorited = s }
                var next = list
                next[j] = r
                groupServerRecipes[gid] = next
            }
        }
        if var sel = selectedRecipe, sel.id == recipeId {
            if let c = newCatalog { sel.isCatalogStarred = c }
            if let s = newSession { sel.isSessionFavorited = s }
            selectedRecipe = sel
        }
    }

    func loadCombinedGroupPantrySummary() {
        Task {
            isCombinedLoading = true
            do {
                let res = try await groupSharing.fetchCombinedGroupPantry()
                let items = res.items
                let line: String
                if items.isEmpty {
                    line = "No items yet from your group pantries. Add food in the app or run scans."
                } else {
                    let names = items.prefix(20).map { nameForPantryItem($0) }
                    line = names.joined(separator: " · ") + (items.count > 20 ? " …" : "")
                }
                let key = selectedSharedGroup?.id ?? ""
                if !key.isEmpty { combinedPantryLabel[key] = line }
            } catch {
                let key = selectedSharedGroup?.id ?? ""
                if !key.isEmpty { combinedPantryLabel[key] = "Couldn’t load combined pantry." }
            }
            isCombinedLoading = false
        }
    }

    private func nameForPantryItem(_ p: CombinedPantryItemOut) -> String {
        let n: String
        if let t = p.itemName, !t.isEmpty { n = t }
        else if !p.name.isEmpty { n = p.name }
        else { n = "Item" }
        if let w = p.fromUser, !w.isEmpty { return "\(n) (\(w))" }
        if let w = p.userName, !w.isEmpty { return "\(n) (\(w))" }
        if let w = p.contributedBy, !w.isEmpty { return "\(n) (\(w))" }
        return n
    }

    func addCombinedGroupMealIdeas() {
        guard let g = selectedSharedGroup else {
            showInfo("Select a group first.", isError: true)
            return
        }
        Task {
            isLoading = true
            do {
                let res = try await groupSharing.requestCombinedGroupMeal(count: 6)
                let fromAi = res.recipes.map { mapSessionRecipeToShared($0) }
                var current = groupServerRecipes[g.id] ?? self.groups.first(where: { $0.id == g.id })?.recipes ?? []
                for r in fromAi {
                    if !current.contains(where: { $0.id == r.id }) { current.insert(r, at: 0) }
                }
                groupServerRecipes[g.id] = current
                if let i = self.groups.firstIndex(where: { $0.id == g.id && !$0.isPersonal }) {
                    self.groups[i].recipes = current
                }
                showInfo("Added group meal ideas from your combined kitchens.")
            } catch {
                showInfo("Couldn’t get group meal ideas. \(error.localizedDescription)", isError: true)
            }
            isLoading = false
        }
    }

    private func mapSessionRecipeToShared(_ r: SessionRecipeOut) -> SharedRecipe {
        let u = toKotlinStringList((r as AnyObject).value(forKey: "uses")) ?? []
        let e = toKotlinStringList((r as AnyObject).value(forKey: "extra")) ?? []
        let steps = toKotlinStringList((r as AnyObject).value(forKey: "steps")) ?? []
        let have = u.map { "\($0) (from group)" }
        let nameOpt = (r as AnyObject).value(forKey: "name") as? String
        let titleOpt = (r as AnyObject).value(forKey: "title") as? String
        let name: String
        if let n = nameOpt?.trimmingCharacters(in: .whitespacesAndNewlines), !n.isEmpty { name = n }
        else if let t = titleOpt?.trimmingCharacters(in: .whitespacesAndNewlines), !t.isEmpty { name = t }
        else { name = "Meal" }
        let rid = intFromK((r as AnyObject).value(forKey: "id"))
        let fav = (r as AnyObject).value(forKey: "favorited") as? Bool
        return SharedRecipe(
            id: "gmeal-\(rid)-\(name.hashValue)",
            title: name,
            description: "From everyone’s combined pantry in your groups.",
            ownerName: "AI Suggestion",
            missingItems: e,
            availableItems: have,
            instructions: steps,
            perishableProducts: [],
            serverSharedRecipeId: nil,
            sessionRecipeId: rid,
            isSessionFavorited: fav
        )
    }

    func loadGroupDetail(id: String) async {
        isDetailLoading = true
        guard let idInt = Int32(id) else {
            isDetailLoading = false
            return
        }
        do {
            let detail = try await apiService.fetchGroupDetail(id: idInt)
            let currentUserId = AuthManager.shared.currentUser?.id
            let members = detail.members.map { m in
                GroupMember(
                    realId:     m.user.id,
                    name:       m.user.id == currentUserId ? "You" : m.user.name,
                    avatarSeed: String(detail.id)
                )
            }
            
            let owner = detail.members.first { Int($0.user.id) == Int(detail.createdByUserId) }
            let ownerName = (owner?.user.id == currentUserId) ? "You" : owner?.user.name
            
            groups = groups.map { g in
                guard g.id == id else { return g }
                var updated = g
                updated.code    = detail.code
                updated.members = members
                if let name = ownerName {
                    updated.ownerName = name
                }
                return updated
            }
        } catch {
        }
        isDetailLoading = false
    }


    func selectGroup(_ id: String) {
        selectedId = id
        if !id.hasPrefix("group_") && id != "personal" {
            Task { await loadGroupDetail(id: id) }
            loadCombinedGroupPantrySummary()
        }
    }
    
    func openRecipe(_ recipe: SharedRecipe) {
        selectedRecipe = recipe
    }
    
    func closeRecipeDetails() {
        selectedRecipe = nil
    }


    func openDialog(_ mode: GroupDialogMode) { dialogMode = mode }
    func closeDialog() { dialogMode = nil }

    func joinGroup() {
        guard !isLoading else { return }
        let code = joinCodeInput.trimmingCharacters(in: .whitespaces)
        guard code.count >= 4 else {
            showInfo("Please enter a valid group code.", isError: true)
            dialogMode   = nil
            joinCodeInput = ""
            return
        }
        if let existing = groups.first(where: { $0.code == code }) {
            selectedId    = existing.id
            showInfo("You are already in this group.")
            dialogMode    = nil
            joinCodeInput = ""
            return
        }
        Task {
            isLoading = true
            do {
                let joined = try await apiService.joinGroup(code: code)
                showInfo("Joined group \(joined.name).")
                dialogMode    = nil
                joinCodeInput = ""
                await loadGroups()
                selectedId = String(joined.id)
            } catch {
                showInfo("Invalid code or join failed.", isError: true)
                dialogMode    = nil
                joinCodeInput = ""
            }
            isLoading = false
        }
    }
    
    func createGroup() {
        guard !isLoading else { return }
        let name = createNameInput.trimmingCharacters(in: .whitespaces)
        guard !name.isEmpty else {
            showInfo("Group name cannot be empty.", isError: true)
            dialogMode      = nil
            createNameInput = ""
            return
        }
        Task {
            isLoading = true
            do {
                let created = try await apiService.createGroup(name: name)
                showInfo("Group '\(created.name)' created!")
                dialogMode      = nil
                createNameInput = ""
                await loadGroups()
                selectedId = String(created.id)
            } catch {
                showInfo("Failed to create group.", isError: true)
                dialogMode      = nil
                createNameInput = ""
            }
            isLoading = false
        }
    }

    func leaveGroup() {
        guard let group = selectedGroup else { return }
        if group.isAdmin {
            showInfo("Admins cannot leave their own group. Delete it instead.", isError: true)
            return
        }
        if group.isPersonal || group.id.hasPrefix("group_") {
            showInfo("Personal or demo groups cannot be left.", isError: true)
            return
        }
        guard let groupIdInt = Int32(group.id) else { return }
        Task {
            isLoading = true
            do {
                try await apiService.leaveGroup(id: groupIdInt)
                selectedId = "personal"
                showInfo("You left \(group.name).")
                await loadGroups()
            } catch {
                showInfo("Failed to leave group.", isError: true)
            }
            isLoading = false
        }
    }

    func deleteGroup() {
        guard let group = selectedGroup else { return }
        guard group.isAdmin else {
            showInfo("Only the group admin can delete this group.", isError: true)
            return
        }
        if group.isPersonal || group.id.hasPrefix("group_") {
            showInfo("Personal groups cannot be deleted.", isError: true)
            return
        }
        guard let groupIdInt = Int32(group.id) else { return }
        Task {
            isLoading = true
            do {
                try await apiService.deleteGroup(id: groupIdInt)
                selectedId = "personal"
                showInfo("Group \(group.name) deleted.")
                await loadGroups()
            } catch {
                showInfo("Failed to delete group.", isError: true)
            }
            isLoading = false
        }
    }
    
    func clearInfoMessage() {
        infoMessage = nil
        isError     = false
    }

    func kickGroupMember(id: Int32) {
        guard let group = selectedSharedGroup, let groupIdInt = Int32(group.id) else { return }
        Task {
            isLoading = true
            do {
                try await apiService.kickMember(groupId: groupIdInt, userId: id)
                showInfo("Member removed.")
                await loadGroupDetail(id: group.id)
            } catch {
                showInfo("Failed to remove member.", isError: true)
            }
            isLoading = false
        }
    }

    private func showInfo(_ msg: String, isError: Bool = false) {
        self.infoMessage = msg
        self.isError     = isError
        infoTask?.cancel()
        infoTask = Task {
            try? await Task.sleep(nanoseconds: 2_500_000_000)
            guard !Task.isCancelled else { return }
            self.infoMessage = nil
            self.isError     = false
        }
    }
}
