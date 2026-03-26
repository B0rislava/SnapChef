//
//  AuthView.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//
import SwiftUI

enum AuthDestination: Int {
    case signIn, signUp
}

struct AuthView: View {
    @State private var current: AuthDestination = .signIn
    @State private var previous: AuthDestination = .signIn

    var body: some View {
        ZStack {
            switch current {
            case .signIn:
                SignInView(
                    onSignIn: { /* TODO: navigate to main app */ },
                    onSignUp: { navigate(to: .signUp) }
                )
                .transition(transition(to: .signIn, from: previous))

            case .signUp:
                SignUpView(
                    onBack:   { navigate(to: .signIn) },
                    onSignUp: { /* TODO: navigate to main app */ },
                    onSignIn: { navigate(to: .signIn) }
                )
                .transition(transition(to: .signUp, from: previous))
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
