//
//  ScreenWrapper.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//

import SwiftUI

struct ScreenWrapper: View {
    @State private var currentTab: MainTab = .home

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.greenSecondary
                .ignoresSafeArea()

            ZStack {
                HomeView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .opacity(currentTab == .home ? 1 : 0)

                ProfileView(
                    userName: "John Doe",
                    userEmail: "john.doe@snapchef.app",
                    profileImageUri: nil,
                    onBack: { currentTab = .home },
                    onLogout: { },
                    onDeleteAccount: { },
                    onEditProfile: { }
                )
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .opacity(currentTab == .profile ? 1 : 0)
            }

            SnapChefBottomBar(currentTab: $currentTab)
        }
        .ignoresSafeArea(edges: .bottom)
    }
}
