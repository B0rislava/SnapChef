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
    private var infoTask: Task<Void, Never>?

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
            
        RecipeStore.shared.$sharedRecipes
            .receive(on: DispatchQueue.main)
            .sink { [weak self] recipes in
                guard let self = self else { return }
                for i in 0..<self.groups.count {
                    if !self.groups[i].isPersonal && !self.groups[i].id.hasPrefix("group_") {
                        self.groups[i].recipes = recipes
                    }
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
                return AppGroup(
                    id: String(g.id),
                    name: g.name,
                    code: g.code,
                    ownerName: isAdmin ? "You" : nil,
                    members: [],
                    isAdmin: isAdmin,
                    recipes: RecipeStore.shared.sharedRecipes,
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
            if let id = selectedId, id != "personal" && !id.hasPrefix("group_") {
                await loadGroupDetail(id: id)
            } else if let firstSharedId = remoteGroups.first?.id {
                await loadGroupDetail(id: firstSharedId)
            }
        } catch {
            showInfo("Failed to load groups.", isError: true)
        }
        isLoading = false
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
        let code = joinCodeInput.trimmingCharacters(in: .whitespaces).uppercased()
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
