//
//  SignInViewModel.swift
//  iosApp
//
//  Created by gergana on 3/28/26.
//

import Foundation
import Shared  

@MainActor
final class SignInViewModel: ObservableObject {

    @Published var email:         String  = ""
    @Published var password:      String  = ""
    @Published var showPassword:  Bool    = false
    @Published var isLoading:     Bool    = false
    @Published var errorMessage:  String? = nil

    private let apiService = SnapChefServiceLocator.shared.authApiService

    func updateEmail(_ value: String) {
        email        = value
        errorMessage = nil
    }

    func updatePassword(_ value: String) {
        password     = value
        errorMessage = nil
    }

    func toggleShowPassword() {
        showPassword.toggle()
    }

    func signIn(
        onSuccess:        @escaping () -> Void,
        onVerifyRequired: @escaping (String) -> Void
    ) {
        let trimmedEmail = email.trimmingCharacters(in: .whitespaces)

        guard !trimmedEmail.isEmpty, !password.isEmpty else {
            errorMessage = "Please enter both email and password"
            return
        }

        isLoading    = true
        errorMessage = nil

        Task {
            do {
                let response = try await apiService.login(
                    request: LoginRequest(email: trimmedEmail, password: password)
                )
                AuthManager.shared.accessToken = response.accessToken
                AuthManager.shared.currentUser = response.user
                onSuccess()
            } catch let error as KotlinException where isHttp403(error) {
                onVerifyRequired(trimmedEmail)
            } catch {
                isLoading    = false
                errorMessage = "Login failed. Check your credentials."
            }
        }
    }
    
    func googleSignIn(
        idToken:   String,
        onSuccess: @escaping () -> Void
    ) {
        isLoading    = true
        errorMessage = nil

        Task {
            do {
                let response = try await apiService.googleAuth(idToken: idToken)
                AuthManager.shared.accessToken = response.accessToken
                AuthManager.shared.currentUser = response.user
                onSuccess()
            } catch {
                isLoading    = false
                errorMessage = "Google Sign-In failed on the server."
            }
        }
    }

    private func isHttp403(_ error: KotlinException) -> Bool {
        return error.description().contains("403")
    }
}
