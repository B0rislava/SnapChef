//
//  EditProfileViewModel.swift
//  iosApp
//
//  Created by gergana on 3/28/26.
//

import Foundation
import Combine


struct EditProfileUiState {
    var editedName:            String  = ""
    var editedEmail:           String  = ""
    var editedPassword:        String  = ""
    var editedConfirmPassword: String  = ""
    var errorMessage:          String? = nil

    var initials: String {
        let parts = editedName
            .trimmingCharacters(in: .whitespaces)
            .components(separatedBy: .whitespaces)
            .filter { !$0.isEmpty }
        guard !parts.isEmpty else { return "JD" }
        if parts.count == 1 { return String(parts[0].prefix(2)).uppercased() }
        return (String(parts[0].first!) + String(parts[parts.count - 1].first!)).uppercased()
    }
}

@MainActor
final class EditProfileViewModel: ObservableObject {

    @Published private(set) var uiState = EditProfileUiState()

    func setInitialValues(name: String, email: String) {
        guard uiState.editedName != name || uiState.editedEmail != email else { return }
        uiState.editedName  = name
        uiState.editedEmail = email
    }

    func updateName(_ value: String) {
        uiState.editedName   = value
        uiState.errorMessage = nil
    }

    func updateEmail(_ value: String) {
        uiState.editedEmail  = value
        uiState.errorMessage = nil
    }

    func updatePassword(_ value: String) {
        uiState.editedPassword = value
        uiState.errorMessage   = nil
    }

    func updateConfirmPassword(_ value: String) {
        uiState.editedConfirmPassword = value
        uiState.errorMessage          = nil
    }

    func validateAndSave(onValidSave: (String, String, String, String) -> Void) {
        let name            = uiState.editedName.trimmingCharacters(in: .whitespaces)
        let email           = uiState.editedEmail.trimmingCharacters(in: .whitespaces)
        let password        = uiState.editedPassword
        let confirmPassword = uiState.editedConfirmPassword

        guard !name.isEmpty, !email.isEmpty else {
            uiState.errorMessage = "Name and email cannot be empty."
            return
        }

        guard name.count >= 2 else {
            uiState.errorMessage = "Name must be at least 2 characters long."
            return
        }

        let emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        guard email.range(of: emailRegex, options: .regularExpression) != nil else {
            uiState.errorMessage = "Please enter a valid email address."
            return
        }

        if !password.isEmpty {
            guard password.count >= 8 else {
                uiState.errorMessage = "Password must be at least 8 characters long."
                return
            }
            guard password == confirmPassword else {
                uiState.errorMessage = "Passwords do not match."
                return
            }
        }

        uiState.errorMessage = nil
        onValidSave(name, email, password, confirmPassword)
    }

    func applyBackendJson(_ json: String) {
        guard let data = json.data(using: .utf8),
              let obj  = try? JSONSerialization.jsonObject(with: data) as? [String: Any]
        else { return }
        if let name  = obj["name"]  as? String { uiState.editedName  = name  }
        if let email = obj["email"] as? String { uiState.editedEmail = email }
    }
}
