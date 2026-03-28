//
//  GroupsViewModel.swift
//  iosApp
//
//  Created by gergana on 3/27/26.
//
import Foundation
import Shared

// MARK: - Local models

struct GroupMember: Identifiable {
    let id   = UUID()
    let name: String
    let avatarSeed: String
}

struct AppGroup: Identifiable {
    let id:          Int
    var name:        String
    var code:        String?
    var ownerName:   String?   // "You" if current user created it
    var members:     [GroupMember]
    var isAdmin:     Bool      // true when ownerName == "You"
}

// MARK: - Dialog mode

enum GroupDialogMode: Identifiable {
    case choice, join, create
    var id: Int {
        switch self { case .choice: return 0; case .join: return 1; case .create: return 2 }
    }
}

// MARK: - ViewModel

@MainActor
final class GroupsViewModel: ObservableObject {

    // Published state
    @Published private(set) var groups:        [AppGroup]        = []
    @Published              var selectedId:    Int?              = nil
    @Published              var dialogMode:    GroupDialogMode?  = nil
    @Published              var joinCodeInput: String            = ""
    @Published              var createNameInput: String          = ""
    @Published              var isLoading:     Bool              = false
    @Published              var infoMessage:   String?           = nil
    @Published              var isError:       Bool              = false

    // Derived
    var selectedGroup: AppGroup? {
        guard let id = selectedId else { return groups.first }
        return groups.first { $0.id == id } ?? groups.first
    }

    private let apiService = SnapChefServiceLocator.shared.authApiService
    private var infoTask: Task<Void, Never>?

    init() {
        Task { await loadGroups() }
    }

    // MARK: - Load

    func loadGroups() async {
        isLoading = true
        do {
            let raw = try await apiService.fetchGroups()
            let currentUserId = AuthManager.shared.currentUser?.id
            groups = raw.map { g in
                let isAdmin = (g.createdByUserId == currentUserId)
                return AppGroup(
                    id:        Int(g.id),
                    name:      g.name,
                    code:      g.code,
                    ownerName: isAdmin ? "You" : nil,
                    members:   [],
                    isAdmin:   isAdmin
                )
            }
            // Auto-select first group
            if selectedId == nil, let first = groups.first {
                selectedId = first.id
            }
            // Fetch detail for selected group
            if let id = selectedId {
                await loadGroupDetail(id: id)
            }
        } catch {
            showInfo("Failed to load groups.", isError: true)
        }
        isLoading = false
    }

    func loadGroupDetail(id: Int) async {
        do {
            let detail = try await apiService.fetchGroupDetail(id: Int32(id))
            let currentUserId = AuthManager.shared.currentUser?.id
            let members = detail.members.map { m in
                GroupMember(
                    name:       m.user.id == currentUserId ? "You" : m.user.name,
                    avatarSeed: m.user.name
                )
            }
            groups = groups.map { g in
                guard g.id == id else { return g }
                var updated = g
                updated.code    = detail.code
                updated.members = members
                return updated
            }
        } catch {
            // silently ignore detail fetch errors
        }
    }

    // MARK: - Selection

    func selectGroup(_ id: Int) {
        selectedId = id
        Task { await loadGroupDetail(id: id) }
    }

    // MARK: - Dialogs

    func openDialog(_ mode: GroupDialogMode) { dialogMode = mode }
    func closeDialog() { dialogMode = nil }

    // MARK: - Join

    func joinGroup() {
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
                selectedId = Int(joined.id)
            } catch {
                showInfo("Invalid code or join failed.", isError: true)
                dialogMode    = nil
                joinCodeInput = ""
            }
            isLoading = false
        }
    }

    // MARK: - Create

    func createGroup() {
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
                selectedId = Int(created.id)
            } catch {
                showInfo("Failed to create group.", isError: true)
                dialogMode      = nil
                createNameInput = ""
            }
            isLoading = false
        }
    }

    // MARK: - Leave

    func leaveGroup() {
        guard let group = selectedGroup else { return }
        if group.isAdmin {
            showInfo("Admins cannot leave their own group. Delete it instead.", isError: true)
            return
        }
        Task {
            isLoading = true
            do {
                try await apiService.leaveGroup(id: Int32(group.id))
                selectedId = nil
                showInfo("You left \(group.name).")
                await loadGroups()
            } catch {
                showInfo("Failed to leave group.", isError: true)
            }
            isLoading = false
        }
    }

    // MARK: - Delete

    func deleteGroup() {
        guard let group = selectedGroup else { return }
        guard group.isAdmin else {
            showInfo("Only the group admin can delete this group.", isError: true)
            return
        }
        Task {
            isLoading = true
            do {
                try await apiService.deleteGroup(id: Int32(group.id))
                selectedId = nil
                showInfo("Group \(group.name) deleted.")
                await loadGroups()
            } catch {
                showInfo("Failed to delete group.", isError: true)
            }
            isLoading = false
        }
    }

    // MARK: - Helpers

    func clearInfoMessage() {
        infoMessage = nil
        isError     = false
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
