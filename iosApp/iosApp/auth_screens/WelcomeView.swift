//
//  WelcomeView.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//
import SwiftUI

struct WelcomeView: View {
    var onGetStarted: () -> Void = {}
    var onSignIn:     () -> Void = {}

    @State private var leafOffset: CGFloat = -6

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color.greenBackground, Color.greenSecondary.opacity(0.45)],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            Circle()
                .fill(Color.greenSecondary.opacity(0.35))
                .frame(width: 260, height: 260)
                .offset(x: -130, y: -300)

            Circle()
                .fill(Color.greenPrimary.opacity(0.12))
                .frame(width: 160, height: 160)
                .offset(x: 140, y: -320)

            VStack(spacing: 0) {
                Spacer()

                ZStack {
                    Circle()
                        .fill(Color.greenPrimary)
                        .frame(width: 140, height: 140)
                        .shadow(color: .black.opacity(0.2), radius: 16, x: 0, y: 8)

                    Text("🍽️")
                        .font(.system(size: 64))
                }
                .offset(y: leafOffset)
                .onAppear {
                    withAnimation(
                        .easeInOut(duration: 2.4)
                        .repeatForever(autoreverses: true)
                    ) {
                        leafOffset = 6
                    }
                }

                Spacer().frame(height: 40)

                Text(NSLocalizedString("snapchef", comment: ""))
                    .font(.system(size: 36, weight: .heavy))
                    .foregroundColor(Color.greenPrimary)

                Spacer().frame(height: 12)

                Text(NSLocalizedString("welcome_text", comment: ""))
                    .font(.system(size: 16))
                    .foregroundColor(Color.greenOnBackground.opacity(0.65))
                    .multilineTextAlignment(.center)

                Spacer()

                HStack(spacing: 6) {
                    Circle()
                        .fill(Color.greenPrimary)
                        .frame(width: 10, height: 10)
                    Circle()
                        .fill(Color.greenSecondary)
                        .frame(width: 7, height: 7)
                    Circle()
                        .fill(Color.greenSecondary)
                        .frame(width: 7, height: 7)
                }

                Spacer().frame(height: 32)

                Button(action: onGetStarted) {
                    Text("Get Started")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(Color.greenPrimary)
                        .clipShape(Capsule())
                        .shadow(color: Color.greenPrimary.opacity(0.4), radius: 8, x: 0, y: 4)
                }

                Spacer().frame(height: 16)

                // sign-in link
                HStack(spacing: 0) {
                    Text(NSLocalizedString("already_have_account", comment: ""))
                        .font(.system(size: 14))
                        .foregroundColor(Color.greenOnBackground.opacity(0.6))

                    Button(action: onSignIn) {
                        Text(NSLocalizedString("login", comment: ""))
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(Color.greenPrimary)
                    }
                }

                Spacer().frame(height: 32)
            }
            .padding(.horizontal, 32)
        }
    }
}
