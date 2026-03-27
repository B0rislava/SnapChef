//
//  EditProfileVIewModel.swift
//  iosApp
//
//  Created by gergana on 3/27/26.
//

import Foundation
import Combine

struct EditProfileUiState {
    var editedName: String = ""
    var editedEmail: String = ""
    
    var initials: String {
        let parts = editedName.trimmingCharacters(in: .whitespacesAndNewlines)
            .components(separatedBy: .whitespacesAndNewlines)
            .filter { !$0.isEmpty }
        
        if parts.isEmpty { return "JD" }
        if parts.count == 1 { return String(parts.first!.prefix(2)).uppercased() }
        
        let first = parts.first!.first!
        let last = parts.last!.first!
        return "\(first)\(last)".uppercased()
    }
}

@MainActor
class EditProfileViewModel: ObservableObject {
    @Published var uiState = EditProfileUiState()
    
    func setInitialValues(name: String, email: String) {
        if uiState.editedName != name || uiState.editedEmail != email {
            uiState.editedName = name
            uiState.editedEmail = email
        }
    }
    
    func updateName(_ value: String) {
        uiState.editedName = value
    }
    
    func updateEmail(_ value: String) {
        uiState.editedEmail = value
    }
    
    func applyBackendJson(json: String) {
        guard let data = json.data(using: .utf8),
              let dict = try? JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            return
        }
        
        if let newName = dict["name"] as? String {
            uiState.editedName = newName
        }
        if let newEmail = dict["email"] as? String {
            uiState.editedEmail = newEmail
        }
    }
}
