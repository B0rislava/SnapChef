
import Foundation
import Combine
import Shared

struct ProfileInventoryItem: Identifiable {
    let id         = UUID()
    let backendId: Int
    let name:      String
    let category:  String
    let quantity:  String
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
                inventoryItems = items.map { item in
                    let qty: String
                    if let unit = item.unit, !unit.isEmpty {
                        qty = "\(item.quantity) \(unit)"
                    } else {
                        qty = "\(item.quantity)"
                    }
                    return ProfileInventoryItem(
                        backendId: Int(item.id),
                        name:     item.name,
                        category: categoryFromSource(item.source),
                        quantity: qty
                    )
                }
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
    func updateProfile(name: String, email: String, newPassword: String) async -> Bool {
        isLoading    = true
        errorMessage = nil
        let pw: String? = newPassword.isEmpty ? nil : newPassword
        let request = UserUpdateRequest(name: name, email: email, password: pw)
        do {
            let u = try await authApiService.updateMe(request: request)
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

    func removePantryItem(backendId: Int) async {
        isLoading    = true
        errorMessage = nil
        do {
            try await authApiService.deletePantryItem(id: Int32(backendId))
            inventoryItems.removeAll { $0.backendId == backendId }
        } catch {
            errorMessage = "Could not remove item. \(error.localizedDescription)"
        }
        isLoading = false
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
