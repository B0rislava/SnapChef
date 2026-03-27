//
//  SignUpViewModel.swift
//  iosApp
//
//  Created by gergana on 3/27/26.
//

import Foundation
import Shared

@MainActor
class SignUpViewModel: ObservableObject {
    
    @Published var isLoading = false
    @Published var errorMessage: String? = nil
    @Published var isSuccess = false
    
    private let authService = SnapChefServiceLocator.shared.authApiService

    private func isValidEmail(_ email: String) -> Bool {
        let emailRegex = #"^[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}$"#
        return email.range(of: emailRegex, options: .regularExpression) != nil
    }
    
    func signUp(name: String, email: String, password: String) {
        let trimmedName = name.trimmingCharacters(in: .whitespaces)
        let trimmedEmail = email.trimmingCharacters(in: .whitespaces)

        guard !trimmedName.isEmpty, !trimmedEmail.isEmpty, !password.isEmpty else {
            self.errorMessage = "All fields are required."
            return
        }

        guard trimmedName.count >= 2 else {
            self.errorMessage = "Name must be at least 2 characters"
            return
        }

        guard isValidEmail(trimmedEmail) else {
            self.errorMessage = "Please enter a valid email address"
            return
        }

        guard password.count >= 8 else {
            self.errorMessage = "Password must be at least 8 characters"
            return
        }
        
        isLoading = true
        errorMessage = nil
        
        let request = SignupRequest(email: trimmedEmail, name: trimmedName, password: password)
        
        Task {
            do {
                let _ = try await authService.signup(request: request)
                self.isSuccess = true
                self.isLoading = false
            } catch {
                let errString = String(describing: error)
                if errString.contains("403") || errString.contains("401") {
                    self.isSuccess = true
                } else if errString.contains("409")
                            || errString.contains("Conflict")
                            || errString.contains("already exists")
                            || errString.contains("already registered") {
                    self.errorMessage = "An account with this email already exists. Try signing in instead."
                } else {
                    self.errorMessage = "Something went wrong. Please try again."
                    print(error.localizedDescription)
                }
                self.isLoading = false
            }
        }
    }
}
