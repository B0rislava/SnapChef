//
//  GroupsViewModel.swift
//  iosApp
//
//  Created by gergana on 3/27/26.
//
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
    /// Server-backed recipes for each real group (not the fake "personal" row).
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
            
            // Determine which group detail to load
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
            let out = try await groupSharing.listSharedRecipesForGroup(groupId: gid)
            return out.map { self.mapGroupSharedOut($0) }
        } catch {
            return groupServerRecipes[groupId] ?? []
        }
    }

    private func mapGroupSharedOut(_ o: GroupSharedRecipeOut) -> SharedRecipe {
        let title: String
        if let t = o.title, !t.isEmpty { title = t }
        else if let n = o.name, !n.isEmpty { title = n }
        else { title = "Recipe" }
        var desc: String = ""
        if let d = (o as AnyObject).value(forKey: "description_") as? String, !d.isEmpty { desc = d }
        else if let b = o.body, !b.isEmpty { desc = b }
        let steps: [String] = toKotlinStringList((o as AnyObject).value(forKey: "instructions")) ?? []
        let altSteps: [String] = toKotlinStringList((o as AnyObject).value(forKey: "steps")) ?? []
        let instructions = !steps.isEmpty ? steps : altSteps
        let ings: [String] = toKotlinStringList((o as AnyObject).value(forKey: "ingredients")) ?? []
        let have = ings.map { "\($0) (from group)" }
        let who: String
        if let a = o.authorName, !a.isEmpty { who = a }
        else if let a = o.ownerName, !a.isEmpty { who = a }
        else if let a = o.sharedBy, !a.isEmpty { who = a }
        else if let a = o.sharerName, !a.isEmpty { who = a }
        else { who = "Group member" }
        let sid = intFromK(o.id)
        return SharedRecipe(
            id: "srv-\(sid)",
            title: title,
            description: desc,
            ownerName: who,
            missingItems: [],
            availableItems: have,
            instructions: instructions,
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

    func shareRecipeToGroup(_ recipe: SharedRecipe, to group: AppGroup) {
        if group.isPersonal {
            showInfo("Pick a real group, not “Your recipes”.", isError: true)
            return
        }
        guard let gid = Int32(group.id) else { return }
        Task {
            isLoading = true
            do {
                let ings = recipe.allIngredientPhrasesForShare()
                let _ = try await groupSharing.shareRecipeToGroup(
                    request: ShareRecipeToGroupRequest(
                        groupId: gid,
                        title: recipe.title,
                        description: recipe.description,
                        ingredients: ings,
                        instructions: recipe.instructions,
                        recipeId: nil,
                        sessionRecipeId: nil
                    )
                )
                let list = await fetchSharedRecipesList(groupId: group.id)
                groupServerRecipes[group.id] = list
                if let i = self.groups.firstIndex(where: { $0.id == group.id }) {
                    self.groups[i].recipes = list
                }
                showInfo("Shared to \(group.name).")
            } catch {
                showInfo("Could not share recipe. \(error.localizedDescription)", isError: true)
            }
            isLoading = false
        }
    }

    /// Loads combined group pantry (all members’ products) and stores a one-line summary for the selected group card.
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

    /// AI meal ideas using all group members’ pantry items together (`POST /ai/groups/combined-meal`).
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
        let name = (r as AnyObject).value(forKey: "name") as? String ?? "Meal"
        return SharedRecipe(
            id: "gmeal-\(intFromK((r as AnyObject).value(forKey: "id")))-\(name.hashValue)",
            title: name,
            description: "From everyone’s combined pantry in your groups.",
            ownerName: "AI Suggestion",
            missingItems: e,
            availableItems: have,
            instructions: steps,
            perishableProducts: [],
            serverSharedRecipeId: nil
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
