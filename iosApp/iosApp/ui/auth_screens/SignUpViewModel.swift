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
    
    func signUp(name: String, email: String, password: String) {
        guard !name.trimmingCharacters(in: .whitespaces).isEmpty,
              !email.trimmingCharacters(in: .whitespaces).isEmpty,
              !password.isEmpty else {
            self.errorMessage = "All fields are required."
            return
        }
        
        isLoading = true
        errorMessage = nil
        
        let request = SignupRequest(email: email, name: name, password: password)
        
        Task {
            do {
                let response = try await authService.signup(request: request)
                
                self.isSuccess = true
                self.isLoading = false
                
            } catch {
                let errString = String(describing: error)
                if errString.contains("403") || errString.contains("401") {
                    self.isSuccess = true 
                } else {
                    self.errorMessage = error.localizedDescription
                }
                self.isLoading = false
            }
        }
    }
}
