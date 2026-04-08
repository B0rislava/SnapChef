//
//  BottomBar.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//

import SwiftUI

enum MainTab {
    case home, recipes, recommended, profile

    var icon: String {
        switch self {
        case .recipes: return "person.3"
        case .home: return "house"
        case .recommended: return "star"
        case .profile: return "person.circle"
        }
    }

    var selectedIcon: String {
        switch self {
        case .recipes: return "person.3.fill"
        case .home: return "house.fill"
        case .recommended: return "star.fill"
        case .profile: return "person.circle.fill"
        }
    }

    var label: String {
        switch self {
        case .recipes: return "Groups"
        case .home: return "Home"
        case .recommended: return "Recommended Recipes"
        case .profile: return "Profile"
        }
    }
}

struct SnapChefBottomBar: View {
    @Binding var currentTab: MainTab

    var body: some View {
        HStack(spacing: 0) {
            ForEach([.recipes,MainTab.home, .recommended, .profile], id: \.label) { tab in
                BottomBarItem(
                    tab: tab,
                    isSelected: currentTab == tab,
                    onTap: { currentTab = tab }
                )
            }
        }
        .padding(.horizontal, 16)
        .frame(height: 64)
        .background(Color.white)
        .clipShape(Capsule())
        .shadow(color: Color.greenPrimary.opacity(0.25), radius: 16, x: 0, y: 4)
        .padding(.horizontal, 48)
        .padding(.bottom, 24)
    }
}

private struct BottomBarItem: View {
    let tab: MainTab
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            ZStack {
                Circle()
                    .fill(isSelected ? Color.greenPrimary : Color.clear)
                    .frame(width: 48, height: 48)

                Image(systemName: isSelected ? tab.selectedIcon : tab.icon)
                    .foregroundColor(isSelected ? .white : Color.greenPrimary)
                    .font(.system(size: 22))
            }
        }
        .buttonStyle(.plain)
        .frame(maxWidth: .infinity)
        .animation(.easeInOut(duration: 0.2), value: isSelected)
    }
}
