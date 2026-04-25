
import Foundation
import Combine
import Shared

struct ProfileInventoryItem: Identifiable {
    var id: String { "\(name.lowercased())|\(quantity)" }
    let pantryItemIds: [Int]
    let name: String
    let category: String
    let quantity: String
}


@MainActor
final class ProfileViewModel: ObservableObject {

    @Published var userName:        String = ""
    @Published var userEmail:       String = ""
    @Published var profileImageUri: URL?   = nil

    @Published var inventoryItems: [ProfileInventoryItem] = []

    @Published var isLoading:    Bool    = false
    @Published var errorMessage: String? = nil

    private let authApiService = SnapChefServiceLocator.shared.authApiService


    init() {
        loadUserFromAuthManager()
        loadInventory()
    }


    func loadUserFromAuthManager() {
        guard let user = AuthManager.shared.currentUser else { return }
        userName  = user.name
        userEmail = user.email
    }

    func loadInventory() {
        isLoading    = true
        errorMessage = nil
        Task {
            do {
                let items = try await authApiService.fetchPantryItems()
                let grouped = Dictionary(grouping: items) {
                    $0.name.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
                }
                inventoryItems = grouped.values.compactMap { group -> ProfileInventoryItem? in
                    guard let first = group.first else { return nil }
                    let displayName = first.name.trimmingCharacters(in: .whitespacesAndNewlines)
                    let totalQty = group.map { p -> Int in
                        let o = p as AnyObject
                        guard let n = o.value(forKey: "quantity") else { return 0 }
                        if let k = n as? KotlinInt { return Int(k.intValue) }
                        if let v = n as? Int { return v }
                        if let v = n as? NSNumber { return v.intValue }
                        return 0
                    }.reduce(0, +)
                    let unit = group.first { u in u.unit != nil && !(u.unit?.isEmpty ?? true) }?.unit
                    let qtyStr: String
                    if let u = unit, !u.isEmpty {
                        qtyStr = "\(totalQty) \(u)"
                    } else {
                        qtyStr = "\(totalQty)"
                    }
                    let cat: String
                    if group.contains(where: { $0.source.lowercased() == "scan" }) {
                        cat = "Scanned"
                    } else if group.contains(where: { $0.source.lowercased() == "manual" }) {
                        cat = "Manual"
                    } else {
                        cat = categoryFromSource(first.source)
                    }
                    return ProfileInventoryItem(
                        pantryItemIds: group.map { Int($0.id) },
                        name: displayName,
                        category: cat,
                        quantity: qtyStr
                    )
                }
                .sorted { $0.name.localizedCaseInsensitiveCompare($1.name) == .orderedAscending }
            } catch {
                errorMessage = "Could not load inventory: \(error.localizedDescription)"
            }
            isLoading = false
        }
    }

    func updateUser(name: String, email: String) {
        userName  = name
        userEmail = email
        if let user = AuthManager.shared.currentUser {
            AuthManager.shared.currentUser = UserOut(
                id:    user.id,
                email: email,
                name:  name
            )
        }
    }

    @discardableResult
    func updateProfile(name: String, email: String, newPassword: String, currentPassword: String) async -> Bool {
        isLoading    = true
        errorMessage = nil
        do {
            let u = try await authApiService.updateMe(
                request: UserUpdateRequest(name: name, email: email, password: nil)
            )
            if !newPassword.isEmpty {
                try await authApiService.changePassword(
                    currentPassword: currentPassword,
                    newPassword: newPassword
                )
            }
            userName  = u.name
            userEmail = u.email
            AuthManager.shared.currentUser = u
            isLoading = false
            return true
        } catch {
            errorMessage = error.localizedDescription
            isLoading    = false
            return false
        }
    }

    func removeInventoryGroup(_ item: ProfileInventoryItem) async {
        errorMessage = nil
        for id in item.pantryItemIds {
            do {
                try await authApiService.deletePantryItem(id: Int32(id))
            } catch {
                errorMessage = "Could not remove some items. \(error.localizedDescription)"
            }
        }
        loadInventory()
    }

    func logout() {
        AuthManager.shared.logout()
    }

    func deleteAccount(onSuccess: @escaping () -> Void) {
        isLoading    = true
        errorMessage = nil
        Task {
            do {
                try await authApiService.deleteAccount()
                AuthManager.shared.logout()
                await MainActor.run { onSuccess() }
            } catch {
                errorMessage = error.localizedDescription
            }
            isLoading = false
        }
    }

    private func categoryFromSource(_ source: String) -> String {
        switch source.lowercased() {
        case "scan":   return "Scanned"
        case "manual": return "Manual"
        default:       return source.capitalized
        }
    }
}
