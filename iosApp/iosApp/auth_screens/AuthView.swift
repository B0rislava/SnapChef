//
//  AuthView.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//
import SwiftUI

enum AuthDestination: Int {
    case welcome, signUp, signIn, verify
}

struct AuthView: View {
    var onAuthSuccess: () -> Void = {}

    @State private var current:       AuthDestination = .welcome
    @State private var previous:      AuthDestination = .welcome
    @State private var emailToVerify: String          = ""
    @State private var welcomePage:   Int             = 0

    @StateObject private var signInVM  = SignInViewModel()
    @StateObject private var signUpVM  = SignUpViewModel()
    @StateObject private var verifyVM  = VerificationViewModel()

    var body: some View {
        ZStack {
            switch current {
            case .welcome:
                WelcomeView(
                    onGetStarted: { navigate(to: .signUp) },
                    onSignIn:     { navigate(to: .signIn) },
                    currentPage:  $welcomePage
                )
                .transition(transition(to: .welcome, from: previous))

            case .signIn:
                SignInView(
                    viewModel: signInVM,
                    onBack:   { navigate(to: .welcome) },
                    onSignIn: { onAuthSuccess() },
                    onSignUp: { navigate(to: .signUp) },
                    onVerifyRequired: { email in
                        emailToVerify = email
                        navigate(to: .verify)
                    }
                )
                .transition(transition(to: .signIn, from: previous))

            case .signUp:
                SignUpView(
                    viewModel: signUpVM,
                    onBack:   { navigate(to: .welcome) },
                    onSignUp: { onAuthSuccess() },
                    onSignIn: { navigate(to: .signIn) },
                    onVerifyRequired: { email in
                        emailToVerify = email
                        navigate(to: .verify)
                    }
                )
                .transition(transition(to: .signUp, from: previous))

            case .verify:
                VerificationView(
                    viewModel: verifyVM,
                    email:     emailToVerify,
                    onBack:    { navigate(to: .signUp) },
                    onSuccess: { onAuthSuccess() }
                )
                .transition(transition(to: .verify, from: previous))
            }
        }
        .animation(.easeInOut(duration: 0.35), value: current)
    }

    private func navigate(to destination: AuthDestination) {
        previous = current
        current  = destination
    }

    private func transition(to: AuthDestination, from: AuthDestination) -> AnyTransition {
        let forward = to.rawValue > from.rawValue
        return .asymmetric(
            insertion: .move(edge: forward ? .trailing : .leading).combined(with: .opacity),
            removal:   .move(edge: forward ? .leading  : .trailing).combined(with: .opacity)
        )
    }
}

struct AuthRootView: View {
    var body: some View {
        SnapChefTheme {
            AuthView()
        }
    }
}
