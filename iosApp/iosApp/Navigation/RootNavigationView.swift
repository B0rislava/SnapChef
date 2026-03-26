//
//  RootNavigationView.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//

import SwiftUI

enum RootDestination {
    case auth
    case home
}

struct RootNavigationView: View {
    @State private var current: RootDestination = .auth

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
                HomeView()
                    .transition(.opacity)
            }
        }
        .animation(.easeInOut(duration: 0.4), value: current)
    }
}
