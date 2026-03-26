//
//  SignInView.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//
import SwiftUI

struct SignInView: View {
    var onBack:   () -> Void = {}
    var onSignIn: () -> Void = {}
    var onSignUp: () -> Void = {}

    @State private var email      = ""
    @State private var password   = ""
    @State private var showPass   = false
    @State private var rememberMe = false

    var body: some View {
        ZStack(alignment: .topTrailing) {

            LinearGradient(
                colors: [Color.greenSecondary.opacity(0.55), Color.greenBackground],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            Circle()
                .fill(Color.greenPrimary.opacity(0.10))
                .frame(width: 220, height: 220)
                .offset(x: 60, y: -50)

            ScrollView(showsIndicators: false) {
                VStack(alignment: .center, spacing: 0) {

                    HStack {
                        Button(action: onBack) {
                            Image(systemName: "arrow.left")
                                .foregroundColor(Color.greenPrimary)
                                .frame(width: 40, height: 40)
                                .background(Color.greenPrimary.opacity(0.10))
                                .clipShape(Circle())
                        }
                        Spacer()
                    }
                    .padding(.top, 16)

                    Spacer().frame(height: 36)

                    Text(NSLocalizedString("welcome_back", comment: ""))
                        .font(.system(size: 26, weight: .heavy))
                        .foregroundColor(Color.greenPrimary)

                    Spacer().frame(height: 6)

                    Text(NSLocalizedString("sign_in_to_continue", comment: ""))
                        .font(.system(size: 14))
                        .foregroundColor(Color.greenOnBackground.opacity(0.55))
                        .multilineTextAlignment(.center)

                    Spacer().frame(height: 40)

                    VStack(spacing: 16) {

                        // email
                        AuthTextField(
                            value: $email,
                            placeholder: NSLocalizedString("enter_your_email", comment: ""),
                            leadingIcon: AnyView(
                                Image(systemName: "envelope")
                                    .foregroundColor(Color.greenPrimary)
                            ),
                            keyboardType: .emailAddress
                        )

                        // password
                        AuthTextField(
                            value: $password,
                            placeholder: NSLocalizedString("enter_your_password", comment: ""),
                            leadingIcon: AnyView(
                                Image(systemName: "lock")
                                    .foregroundColor(Color.greenPrimary)
                            ),
                            trailingIcon: AnyView(
                                Button(action: { showPass.toggle() }) {
                                    Image(systemName: showPass ? "eye.slash" : "eye")
                                        .foregroundColor(Color.greenSecondary)
                                }
                            ),
                            isSecure: !showPass
                        )

                        // remember me + forgot password
                        HStack {
                            HStack(spacing: 4) {
                                Toggle("", isOn: $rememberMe)
                                    .toggleStyle(CheckboxToggleStyle())
                                Text(NSLocalizedString("remember_me", comment: ""))
                                    .font(.system(size: 14))
                                    .foregroundColor(Color.greenOnBackground.opacity(0.65))
                            }
                            Spacer()
                            Button(action: {}) {
                                Text(NSLocalizedString("forgot_password", comment: ""))
                                    .font(.system(size: 14, weight: .semibold))
                                    .foregroundColor(Color.greenPrimary)
                            }
                        }
                    }
                    .padding(24)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 24))
                    .shadow(color: .black.opacity(0.08), radius: 12, x: 0, y: 4)

                    Spacer().frame(height: 24)

                    Button(action: onSignIn) {
                        Text(NSLocalizedString("login", comment: ""))
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                            .background(Color.greenPrimary)
                            .clipShape(Capsule())
                            .shadow(color: Color.greenPrimary.opacity(0.4), radius: 8, x: 0, y: 4)
                    }

                    Spacer().frame(height: 24)

                    OrDivider()

                    Spacer().frame(height: 20)

                    SocialButton(label: NSLocalizedString("continue_google", comment: ""),   emoji: "G")
                    Spacer().frame(height: 12)
                    SocialButton(label: NSLocalizedString("continue_facebook", comment: ""), emoji: "f")

                    Spacer().frame(height: 24)

                    HStack(spacing: 0) {
                        Text(NSLocalizedString("already_have_account", comment: ""))
                            .font(.system(size: 14))
                            .foregroundColor(Color.greenOnBackground.opacity(0.6))

                        Button(action: onSignUp) {
                            Text(NSLocalizedString("create_an_account", comment: ""))
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(Color.greenPrimary)
                        }
                    }

                    Spacer().frame(height: 32)
                }
                .padding(.horizontal, 24)
            }
        }
        .navigationBarHidden(true)
    }
}

struct CheckboxToggleStyle: ToggleStyle {
    func makeBody(configuration: Configuration) -> some View {
        Button(action: { configuration.isOn.toggle() }) {
            Image(systemName: configuration.isOn ? "checkmark.square.fill" : "square")
                .foregroundColor(configuration.isOn ? Color.greenPrimary : Color.greenSecondary)
                .font(.system(size: 20))
        }
        .buttonStyle(.plain)
    }
}
