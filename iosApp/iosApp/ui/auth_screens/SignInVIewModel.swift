//
//  SignInVIewModel.swift
//  iosApp
//
//  Created by gergana on 3/27/26.
//

import Foundation
import Shared
import GoogleSignIn

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

    private func isValidEmail(_ email: String) -> Bool {
        let emailRegex = #"^[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}$"#
        return email.range(of: emailRegex, options: .regularExpression) != nil
    }

    func signIn() {
        guard !email.isEmpty, !password.isEmpty else {
            errorMessage = "Please fill all fields"
            return
        }

        guard isValidEmail(email) else {
            errorMessage = "Please enter a valid email address"
            return
        }

        guard password.count >= 8 else {
            errorMessage = "Password must be at least 8 characters"
            return
        }
        
        isLoading = true
        errorMessage = nil
        
        Task {
            do {
                let request = LoginRequest(email: email, password: password)
                let response = try await SnapChefServiceLocator.shared.authApiService.login(request: request)
                
                AuthManager.shared.signIn(accessToken: response.accessToken, user: response.user)
                
                isLoading = false
                onSignInSuccess?()
            } catch {
                isLoading = false
                let errString = String(describing: error)
                if errString.contains("403") || errString.contains("Forbidden") {
                    onVerifyRequired?(email)
                } else if errString.contains("-1001")
                            || errString.contains("timed out")
                            || errString.contains("SocketTimeout")
                            || errString.contains("CancellationException")
                            || errString.contains("request_timeout")
                            || errString.contains("Timeout") {
                    errorMessage = "The server took too long to respond. Try again (Railway cold start can take a minute)."
                } else if errString.contains("-1004")
                            || errString.contains("Could not connect")
                            || errString.contains("Connection refused")
                            || errString.contains(":61") {
                    errorMessage = "Cannot reach the API. Check your network, or verify BASE_URL in shared BuildKonfig."
                } else {
                    errorMessage = "Invalid email or password"
                    print("Error: \(error)")
                }
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
                    self.onSignInSuccess?()
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
