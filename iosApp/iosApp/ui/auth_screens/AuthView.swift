//
//  AuthView.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//
import SwiftUI

enum AuthDestination: Equatable {
    case welcome
    case signIn
    case signUp
    case verification(email: String)
}

struct AuthView: View {
    var onAuthSuccess: () -> Void = {}

    @State private var current: AuthDestination = .welcome
    @State private var previous: AuthDestination = .welcome

    var body: some View {
        ZStack {
            switch current {
            case .welcome:
                WelcomeView(
                    onGetStarted: { navigate(to: .signUp) },
                    onSignIn:     { navigate(to: .signIn) }
                )
                .transition(transition(to: .welcome, from: previous))
                
            case .signIn:
                SignInView(
                    onBack:   { navigate(to: .welcome) },
                    onSignIn: { onAuthSuccess() },
                    onSignUp: { navigate(to: .signUp) },
                    onVerifyRequired: { email in navigate(to: .verification(email: email)) }
                )
                .transition(transition(to: .signIn, from: previous))

            case .signUp:
                SignUpView(
                    onBack:   { navigate(to: .welcome) },
                    onSignUp: { onAuthSuccess() },
                    onSignIn: { navigate(to: .signIn) },
                    onVerifyRequired: { email in navigate(to: .verification(email: email)) }
                )
                .transition(transition(to: .signUp, from: previous))
                
            case .verification(let email):
                VerificationView(
                    email: email,
                    onBack: { navigate(to: .signUp) },
                    onSuccess: { onAuthSuccess() }
                )
                .transition(transition(to: .verification(email: email), from: previous))
            }
        }
        .animation(.easeInOut(duration: 0.35), value: current)
    }

    private func navigate(to destination: AuthDestination) {
        previous = current
        current  = destination
    }
    
    private func navIndex(for dest: AuthDestination) -> Int {
        switch dest {
        case .welcome: return 0
        case .signIn: return 1
        case .signUp: return 2
        case .verification: return 3
        }
    }
    
    private func transition(to: AuthDestination, from: AuthDestination) -> AnyTransition {
        let forward = navIndex(for: to) > navIndex(for: from)
        return .asymmetric(
            insertion:  .move(edge: forward ? .trailing : .leading).combined(with: .opacity),
            removal:    .move(edge: forward ? .leading  : .trailing).combined(with: .opacity)
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
