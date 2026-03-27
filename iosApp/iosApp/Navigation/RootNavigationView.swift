//
//  RootNavigationView.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//

import SwiftUI
import Shared

enum RootDestination {
    case auth
    case home
}

struct RootNavigationView: View {
    @State private var current: RootDestination

    init() {
        let start: RootDestination = AuthManager.shared.restoreSessionIfValid() ? .home : .auth
        _current = State(initialValue: start)
    }

    var body: some View {
        ZStack {
            switch current {
            case .auth:
                AuthView(onAuthSuccess: {
                    withAnimation(.easeInOut(duration: 0.4)) {
                        current = .home
                    }
                })
                .transition(.opacity)

            case .home:
                ScreenWrapper(onLogout: {
                    AuthManager.shared.logout()
                    withAnimation(.easeInOut(duration: 0.4)) {
                        current = .auth
                    }
                })
                    .transition(.opacity)
            }
        }
        .animation(.easeInOut(duration: 0.4), value: current)
    }
}
