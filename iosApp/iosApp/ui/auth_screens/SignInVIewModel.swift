//
//  SignInVIewModel.swift
//  iosApp
//
//  Created by gergana on 3/27/26.
//

import Foundation
import Shared

@MainActor
class SignInViewModel: ObservableObject {
    @Published var email = ""
    @Published var password = ""
    @Published var rememberMe = false
    @Published var isLoading = false
    @Published var errorMessage: String? = nil
    
    var onBack: (() -> Void)?
    var onSignInSuccess: (() -> Void)?
    var onNavigateToSignUp: (() -> Void)?
    var onVerifyRequired: ((String) -> Void)?

    func navigateBack() { onBack?() }
    func navigateToSignUp() { onNavigateToSignUp?() }

    func signIn() {
        guard !email.isEmpty, !password.isEmpty else {
            errorMessage = "Please fill all fields"
            return
        }
        
        isLoading = true
        errorMessage = nil
        
        Task {
            do {
                let request = LoginRequest(email: email, password: password)
                let apiService = SnapChefServiceLocator.shared.authApiService
                
                let response = try await apiService.login(request: request)
                
                AuthManager.shared.accessToken = response.accessToken
                AuthManager.shared.currentUser = response.user
                
                isLoading = false
                onSignInSuccess?()
            } catch {
                isLoading = false
                let errString = String(describing: error)
                if errString.contains("403") || errString.contains("Forbidden") {
                    onVerifyRequired?(email)
                } else {
                    errorMessage = "Invalid email or password"
                    print("Error: \(error)")
                }
            }
        }
    }
}
