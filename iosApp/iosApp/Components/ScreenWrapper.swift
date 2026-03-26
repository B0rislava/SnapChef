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
     
                HomeView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
     
                SnapChefBottomBar(currentTab: $currentTab)
            }
            .ignoresSafeArea(edges: .bottom)
        }
}
