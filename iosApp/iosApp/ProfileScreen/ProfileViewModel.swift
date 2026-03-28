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

    @Published var inventoryItems: [ProfileInventoryItem] = [
        ProfileInventoryItem(name: "Eggs",           category: "Protein", quantity: "6"),
        ProfileInventoryItem(name: "Cheddar Cheese", category: "Dairy",   quantity: "200 g"),
        ProfileInventoryItem(name: "Tomatoes",       category: "Produce", quantity: "3"),
        ProfileInventoryItem(name: "Chicken Breast", category: "Protein", quantity: "400 g"),
        ProfileInventoryItem(name: "Pasta",          category: "Pantry",  quantity: "500 g"),
        ProfileInventoryItem(name: "Spinach",        category: "Produce", quantity: "100 g"),
        ProfileInventoryItem(name: "Milk",           category: "Dairy",   quantity: "1 L"),
        ProfileInventoryItem(name: "Olive Oil",      category: "Pantry",  quantity: "1 bottle"),
    ]

    @Published var isLoading:    Bool    = false
    @Published var errorMessage: String? = nil

    private let authApiService = SnapChefServiceLocator.shared.authApiService


    init() {
        loadUserFromAuthManager()
    }


    func loadUserFromAuthManager() {
        guard let user = AuthManager.shared.currentUser else { return }
        userName  = user.name
        userEmail = user.email
    }

    func updateUser(name: String, email: String) {
        userName  = name
        userEmail = email
        if var user = AuthManager.shared.currentUser {
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

    func deleteAccount() {
        isLoading    = true
        errorMessage = nil
        Task {
            do {
                try await authApiService.deleteAccount()
                AuthManager.shared.logout()
            } catch {
                errorMessage = error.localizedDescription
            }
            isLoading = false
        }
    }
}
