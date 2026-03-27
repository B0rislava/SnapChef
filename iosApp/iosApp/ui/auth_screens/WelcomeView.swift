import SwiftUI

struct OnboardingPage: Identifiable {
    let id = UUID()
    let icon: String
    let title: String
    let subtitle: String
    let accentColor: Color
}

let onboardingPages = [
    OnboardingPage(
        icon: "fork.knife",
        title: "SnapChef",
        subtitle: "Every day, tonnes of food are thrown away while thousands go hungry. It's time to change that.",
        accentColor: Color.greenPrimary
    ),
    OnboardingPage(
        icon: "camera.fill",
        title: "Snap your fridge",
        subtitle: "Take a photo of whatever ingredients you have left. Our AI instantly recognises every item - no manual typing needed.",
        accentColor: Color.greenPrimary
    ),
    OnboardingPage(
        icon: "leaf.fill",
        title: "Zero Left",
        subtitle: "Get personalised recipes that use exactly what you have. Less waste, more flavour, a better planet.",
        accentColor: Color.greenPrimary
    )
]

struct WelcomeView: View {
    var onGetStarted: () -> Void = {}
    var onSignIn:     () -> Void = {}

    @State private var currentPage = 0
    @State private var floatOffset: CGFloat = -8

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color.greenBackground, Color.greenSecondary.opacity(0.45)],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            Circle()
                .fill(Color.greenSecondary.opacity(0.25))
                .frame(width: 280, height: 280)
                .offset(x: -70, y: -70)

            Circle()
                .fill(Color.greenPrimary.opacity(0.10))
                .frame(width: 180, height: 180)
                .offset(x: 180, y: -40)

            VStack(spacing: 0) {
                Spacer().frame(height: 40)
                
                TabView(selection: $currentPage) {
                    ForEach(0..<onboardingPages.count, id: \.self) { index in
                        let page = onboardingPages[index]
                        
                        VStack(spacing: 0) {
                            Spacer()
                            
                            // Floating icon
                            ZStack {
                                Circle()
                                    .fill(
                                        RadialGradient(
                                            gradient: Gradient(colors: [page.accentColor, page.accentColor.opacity(0.7)]),
                                            center: .center,
                                            startRadius: 0,
                                            endRadius: 70
                                        )
                                    )
                                    .frame(width: 140, height: 140)
                                    .shadow(color: .black.opacity(0.15), radius: 16, x: 0, y: 8)

                                Image(systemName: page.icon)
                                    .font(.system(size: 64))
                                    .foregroundColor(.white)
                            }
                            .offset(y: floatOffset)
                            .onAppear {
                                withAnimation(
                                    .easeInOut(duration: 2.6)
                                    .repeatForever(autoreverses: true)
                                ) {
                                    floatOffset = 8
                                }
                            }

                            Spacer().frame(height: 48)

                            Text(page.title)
                                .font(.system(size: 32, weight: .heavy))
                                .foregroundColor(Color.greenPrimary)
                                .multilineTextAlignment(.center)

                            Spacer().frame(height: 16)

                            Text(page.subtitle)
                                .font(.system(size: 16))
                                .foregroundColor(Color.greenOnBackground.opacity(0.68))
                                .multilineTextAlignment(.center)
                                .padding(.horizontal, 36)
                            
                            Spacer()
                        }
                        .tag(index)
                    }
                }
                .tabViewStyle(PageTabViewStyle(indexDisplayMode: .never))

                // Custom Dots
                HStack(spacing: 8) {
                    ForEach(0..<onboardingPages.count, id: \.self) { index in
                        let isSelected = currentPage == index
                        Capsule()
                            .fill(isSelected ? Color.greenPrimary : Color.greenSecondary.opacity(0.45))
                            .frame(width: isSelected ? 24 : 8, height: 8)
                            .animation(.spring(), value: currentPage)
                    }
                }
                .padding(.bottom, 28)

                // CTA Button
                Button(action: {
                    if currentPage == onboardingPages.count - 1 {
                        onGetStarted()
                    } else {
                        withAnimation {
                            currentPage += 1
                        }
                    }
                }) {
                    Text(currentPage == onboardingPages.count - 1 ? "Get Started" : "Next")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(Color.greenPrimary)
                        .clipShape(Capsule())
                        .shadow(color: Color.greenPrimary.opacity(0.4), radius: 8, x: 0, y: 4)
                }
                .padding(.horizontal, 32)

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
        }
    }
}
