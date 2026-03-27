//
//  WelcomeView.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//
import SwiftUI


private struct OnboardingPage {
    let systemIcon:  String
    let title:       String
    let subtitle:    String
    let accentColor: Color
}

private let pages: [OnboardingPage] = [
    OnboardingPage(
        systemIcon:  "fork.knife",
        title:       "SnapChef",
        subtitle:    "Every day, tonnes of food are thrown away while thousands go hungry. It's time to change that.",
        accentColor: Color.greenPrimary
    ),
    OnboardingPage(
        systemIcon:  "camera.fill",
        title:       "Snap your fridge",
        subtitle:    "Take a photo of whatever ingredients you have left. Our AI instantly recognises every item - no manual typing needed.",
        accentColor: Color.greenPrimary
    ),
    OnboardingPage(
        systemIcon:  "leaf.fill",
        title:       "Zero Left",
        subtitle:    "Get personalised recipes that use exactly what you have. Less waste, more flavour, a better planet.",
        accentColor: Color.greenPrimary
    ),
]


struct WelcomeView: View {
    var onGetStarted: () -> Void = {}
    var onSignIn:     () -> Void = {}

    @StateObject private var viewModel = WelcomeViewModel()

    @State private var currentPage: Int = 0

    @State private var floatOffset: CGFloat = -8

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color.greenBackground, Color.greenSecondary.opacity(0.45)],
                startPoint: .top,
                endPoint:   .bottom
            )
            .ignoresSafeArea()

            Circle()
                .fill(Color.greenSecondary.opacity(0.25))
                .frame(width: 280, height: 280)
                .offset(x: -140, y: -300)

            Circle()
                .fill(Color.greenPrimary.opacity(0.10))
                .frame(width: 180, height: 180)
                .offset(x: 160, y: -320)

            VStack(spacing: 0) {

                TabView(selection: $currentPage) {
                    ForEach(pages.indices, id: \.self) { index in
                        pageContent(for: index)
                            .tag(index)
                    }
                }
                .tabViewStyle(.page(indexDisplayMode: .never))
                .animation(.easeInOut, value: currentPage)

                HStack(spacing: 8) {
                    ForEach(pages.indices, id: \.self) { index in
                        Capsule()
                            .fill(index == currentPage ? Color.greenPrimary : Color.greenSecondary.opacity(0.45))
                            .frame(
                                width:  index == currentPage ? 24 : 8,
                                height: 8
                            )
                            .animation(.easeInOut(duration: 0.25), value: currentPage)
                    }
                }
                .padding(.bottom, 28)

                Button(action: handleCTA) {
                    Text(isLastPage ? "Get Started" : "Next")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(Color.greenPrimary)
                        .clipShape(RoundedRectangle(cornerRadius: 28))
                        .shadow(color: Color.greenPrimary.opacity(0.4), radius: 8, x: 0, y: 4)
                }
                .padding(.horizontal, 32)
                .animation(.easeInOut(duration: 0.2), value: isLastPage)

                if isLastPage {
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
                    .padding(.top, 16)
                    .transition(.opacity.combined(with: .move(edge: .bottom)))
                }

                Spacer().frame(height: 32)
            }
        }
        .onAppear {
            withAnimation(
                .easeInOut(duration: 2.6)
                .repeatForever(autoreverses: true)
            ) {
                floatOffset = 8
            }
        }
    }


    @ViewBuilder
    private func pageContent(for index: Int) -> some View {
        let page = pages[index]

        VStack(spacing: 0) {
            Spacer()

            ZStack {
                Circle()
                    .fill(
                        RadialGradient(
                            colors: [page.accentColor, page.accentColor.opacity(0.7)],
                            center: .center,
                            startRadius: 0,
                            endRadius: 70
                        )
                    )
                    .frame(width: 140, height: 140)
                    .shadow(color: .black.opacity(0.25), radius: 16, x: 0, y: 8)

                Image(systemName: page.systemIcon)
                    .font(.system(size: 64))
                    .foregroundColor(.white)
            }
            .offset(y: floatOffset)

            Spacer().frame(height: 48)

            Text(index == pages.count - 1 ? viewModel.title : page.title)
                .font(.system(size: 34, weight: .heavy))
                .foregroundColor(Color.greenPrimary)
                .multilineTextAlignment(.center)

            Spacer().frame(height: 16)

            Text(index == pages.count - 1 ? viewModel.subtitle : page.subtitle)
                .font(.system(size: 17))
                .foregroundColor(Color.greenOnBackground.opacity(0.68))
                .multilineTextAlignment(.center)
                .lineSpacing(6)

            Spacer()
        }
        .padding(.horizontal, 36)
    }

    private var isLastPage: Bool { currentPage == pages.count - 1 }

    private func handleCTA() {
        if isLastPage {
            onGetStarted()
        } else {
            withAnimation(.easeInOut(duration: 0.35)) {
                currentPage += 1
            }
        }
    }
}
