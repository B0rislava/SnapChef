//
//  SignUpViewModel.swift
//  iosApp
//
//  Created by gergana on 3/28/26.
//

import Foundation
import Shared

@MainActor
final class SignUpViewModel: ObservableObject {

    @Published var name:          String  = ""
    @Published var email:         String  = ""
    @Published var password:      String  = ""
    @Published var showPassword:  Bool    = false
    @Published var agreeTerms:    Bool    = false
    @Published var isLoading:     Bool    = false
    @Published var errorMessage:  String? = nil

    private let apiService = SnapChefServiceLocator.shared.authApiService

    func updateName(_ value: String) {
        name         = value
        errorMessage = nil
    }

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

    func setAgreeTerms(_ value: Bool) {
        agreeTerms = value
    }

    func signUp(
        onVerifyRequired: @escaping (String) -> Void,
        onSuccess:        @escaping () -> Void
    ) {
        let trimmedName  = name.trimmingCharacters(in: .whitespaces)
        let trimmedEmail = email.trimmingCharacters(in: .whitespaces)

        guard !trimmedName.isEmpty, !trimmedEmail.isEmpty, !password.isEmpty else {
            errorMessage = "Please fill out all fields."
            return
        }

        guard trimmedName.count >= 2 else {
            errorMessage = "Name must be at least 2 characters long."
            return
        }

        let emailRegex = #"^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"#
        guard trimmedEmail.range(of: emailRegex, options: .regularExpression) != nil else {
            errorMessage = "Please enter a valid email address."
            return
        }

        guard password.count >= 8 else {
            errorMessage = "Password must be at least 8 characters long."
            return
        }

        guard agreeTerms else {
            errorMessage = "You must agree to the Terms of Service."
            return
        }

        isLoading    = true
        errorMessage = nil

        Task {
            do {
                let response = try await apiService.signup(
                    request: SignupRequest(email: trimmedEmail, name: trimmedName, password: password)
                )
                onVerifyRequired(response.email)
            } catch let error as KotlinException {
                isLoading = false
                let code  = httpStatusCode(from: error)
                switch code {
                case 400, 409:
                    errorMessage = "This email is already registered. Please sign in."
                case 403:
                    onVerifyRequired(trimmedEmail)
                default:
                    errorMessage = "An error occurred during account creation."
                }
            } catch {
                isLoading    = false
                errorMessage = "Network error. Please check your connection."
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

    private func httpStatusCode(from error: KotlinException) -> Int {
        let desc = error.description()
        let numbers = desc.components(separatedBy: CharacterSet.decimalDigits.inverted)
        return numbers.compactMap(Int.init).first(where: { $0 >= 400 && $0 < 600 }) ?? -1
    }
}
