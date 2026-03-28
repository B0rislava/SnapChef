//
//  ProfileViewModel.swift
//  iosApp
//
//  Created by gergana on 3/28/26.
//

import Foundation
import Combine
import Shared

struct ProfileInventoryItem: Identifiable {
    let id       = UUID()
    let name:     String
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

    /// Fetches the user's pantry  from the shared /pantry endpoint.
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
