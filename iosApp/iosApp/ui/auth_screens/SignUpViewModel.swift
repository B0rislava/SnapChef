//
//  SignUpViewModel.swift
//  iosApp
//
//  Created by gergana on 3/27/26.
//

import Foundation
import Shared
import GoogleSignIn

@MainActor
class SignUpViewModel: ObservableObject {
    
    @Published var isLoading = false
    @Published var errorMessage: String? = nil
    @Published var isSuccess = false
    @Published var loggedInDirectly = false
    
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

        Task {
            do {
                let request = SignupRequest(email: trimmedEmail, name: trimmedName, password: password)
                let response = try await authService.signup(request: request)
                
                if response.message == "User registered successfully" || response.message.contains("Verify") {
                    self.isSuccess = true
                    self.loggedInDirectly = false
                } else if response.message.contains("successfully") {
                    self.loggedInDirectly = true
                    self.isSuccess = true
                }
                self.isLoading = false
                
            } catch {
                let errString = String(describing: error)
                if errString.contains("409")
                            || errString.contains("Conflict")
                            || errString.contains("already exists")
                            || errString.contains("already registered") {
                    self.errorMessage = "An account with this email already exists. Try signing in instead."
                } else if errString.contains("-1001")
                            || errString.contains("timed out")
                            || errString.contains("SocketTimeout")
                            || errString.contains("CancellationException")
                            || errString.contains("request_timeout")
                            || errString.contains("Timeout") {
                    self.errorMessage = "The server took too long to respond (Railway may be waking up). Try again in a moment."
                } else if errString.contains("-1004")
                            || errString.contains("Could not connect")
                            || errString.contains("Connection refused")
                            || errString.contains(":61") {
                    self.errorMessage = "Cannot reach the API. Check your network, or verify BASE_URL in shared BuildKonfig matches your backend."
                } else {
                    self.errorMessage = "Something went wrong. Please try again."
                    print(error.localizedDescription)
                }
                self.isLoading = false
            }
        }
    }
    
    func handleGoogleAuth(presenting viewController: UIViewController) {
        isLoading = true
        errorMessage = nil
        
        GIDSignIn.sharedInstance.signIn(withPresenting: viewController) { [weak self] signInResult, error in
                guard let self = self else { return }
            
            if let error = error {
                self.isLoading = false
                self.errorMessage = "Google Sign-In canceled or failed."
                print("Google UI Error: \(error.localizedDescription)")
                return
            }
            
            guard let idToken = signInResult?.user.idToken?.tokenString else {
                self.isLoading = false
                self.errorMessage = "Failed to retrieve Google token."
                return
            }
            
            self.authenticateWithBackend(idToken: idToken)
        }
    }

    private func authenticateWithBackend(idToken: String) {
        Task {
            do {
                let response = try await SnapChefServiceLocator.shared.authApiService.googleAuth(idToken: idToken)
                
                AuthManager.shared.signIn(accessToken: response.accessToken, user: response.user)
                
                DispatchQueue.main.async {
                    self.isLoading = false
                    self.loggedInDirectly = true
                    self.isSuccess = true
                }
            } catch {
                DispatchQueue.main.async {
                    self.isLoading = false
                    self.errorMessage = "Failed to authenticate with our servers."
                    print("Backend Error: \(error)")
                }
            }
        }
    }
}
