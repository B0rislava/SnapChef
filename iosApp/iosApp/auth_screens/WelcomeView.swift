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

    @Binding var currentPage: Int

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
                .offset(x: -140 + CGFloat(currentPage) * 15, y: -300 + CGFloat(currentPage) * 5)
                .animation(.easeInOut(duration: 0.6), value: currentPage)

            Circle()
                .fill(Color.greenPrimary.opacity(0.10))
                .frame(width: 180, height: 180)
                .offset(x: 160 - CGFloat(currentPage) * 20, y: -320 - CGFloat(currentPage) * 10)
                .animation(.easeInOut(duration: 0.6), value: currentPage)

            VStack(spacing: 0) {

                TabView(selection: $currentPage) {
                    ForEach(pages.indices, id: \.self) { index in
                        pageContent(for: index)
                            .tag(index)
                    }
                }
                .tabViewStyle(.page(indexDisplayMode: .never))

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
                .animation(.spring(response: 0.4, dampingFraction: 0.8), value: isLastPage)

                HStack(spacing: 0) {
                    Text(NSLocalizedString("already_have_account", comment: ""))
                        .font(.system(size: 14))
                        .foregroundColor(Color.greenOnBackground.opacity(0.6))

                    Button(action: onSignIn) {
                        Text(NSLocalizedString("login", comment: ""))
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(Color.greenPrimary)
                    }
                    .disabled(!isLastPage)
                }
                .padding(.top, 16)
                .opacity(isLastPage ? 1 : 0)
                .animation(.spring(response: 0.4, dampingFraction: 0.8), value: isLastPage)

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
            .scaleEffect(currentPage == index ? 1 : 0.9)
            .animation(.spring(response: 0.5, dampingFraction: 0.7), value: currentPage)

            Spacer().frame(height: 48)

            Text(page.title)
                .font(.system(size: 34, weight: .heavy))
                .foregroundColor(Color.greenPrimary)
                .multilineTextAlignment(.center)
                .offset(y: currentPage == index ? 0 : 10)
                .animation(.easeOut(duration: 0.5).delay(0.1), value: currentPage)

            Spacer().frame(height: 16)

            Text(page.subtitle)
                .font(.system(size: 17))
                .foregroundColor(Color.greenOnBackground.opacity(0.68))
                .multilineTextAlignment(.center)
                .lineSpacing(6)
                .offset(y: currentPage == index ? 0 : 10)
                .animation(.easeOut(duration: 0.5).delay(0.2), value: currentPage)

            Spacer()
        }
        .padding(.horizontal, 36)
        .id("onboarding-page-\(index)")
    }

    private var isLastPage: Bool { currentPage == pages.count - 1 }

    private func handleCTA() {
        if isLastPage {
            onGetStarted()
        } else {
            withAnimation(.easeInOut(duration: 0.55)) {
                currentPage += 1
            }
        }
    }
}
