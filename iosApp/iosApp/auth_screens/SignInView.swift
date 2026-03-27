//
//  SignInView.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//
import SwiftUI

struct SignInView: View {
    @ObservedObject var viewModel: SignInViewModel

    var onBack:           () -> Void = {}
    var onSignIn:         () -> Void = {}
    var onSignUp:         () -> Void = {}
    var onVerifyRequired: (String) -> Void = { _ in }

    var body: some View {
        ZStack(alignment: .topTrailing) {

            LinearGradient(
                colors: [Color.greenSecondary.opacity(0.55), Color.greenBackground],
                startPoint: .top, endPoint: .bottom
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

                        AuthTextField(
                            value: Binding(
                                get: { viewModel.email },
                                set: { viewModel.updateEmail($0) }
                            ),
                            placeholder: NSLocalizedString("enter_your_email", comment: ""),
                            leadingIcon: AnyView(
                                Image(systemName: "envelope").foregroundColor(Color.greenPrimary)
                            ),
                            keyboardType: .emailAddress
                        )

                        AuthTextField(
                            value: Binding(
                                get: { viewModel.password },
                                set: { viewModel.updatePassword($0) }
                            ),
                            placeholder: NSLocalizedString("enter_your_password", comment: ""),
                            leadingIcon: AnyView(
                                Image(systemName: "lock").foregroundColor(Color.greenPrimary)
                            ),
                            trailingIcon: AnyView(
                                Button(action: viewModel.toggleShowPassword) {
                                    Image(systemName: viewModel.showPassword ? "eye.slash" : "eye")
                                        .foregroundColor(Color.greenSecondary)
                                }
                            ),
                            isSecure: !viewModel.showPassword
                        )

                        HStack {
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

                    Spacer().frame(height: 16)

                    if let error = viewModel.errorMessage {
                        Text(error)
                            .font(.system(size: 13))
                            .foregroundColor(.red)
                            .multilineTextAlignment(.center)
                            .padding(.bottom, 8)
                    }

                    Button(action: {
                        viewModel.signIn(
                            onSuccess:        onSignIn,
                            onVerifyRequired: onVerifyRequired
                        )
                    }) {
                        Group {
                            if viewModel.isLoading {
                                ProgressView().tint(.white)
                            } else {
                                Text(NSLocalizedString("login", comment: ""))
                                    .font(.system(size: 15, weight: .semibold))
                                    .foregroundColor(.white)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(Color.greenPrimary)
                        .clipShape(Capsule())
                        .shadow(color: Color.greenPrimary.opacity(0.4), radius: 8, x: 0, y: 4)
                    }
                    .disabled(viewModel.isLoading)

                    Spacer().frame(height: 24)

                    OrDivider()

                    Spacer().frame(height: 20)

                    SocialButton(label: NSLocalizedString("continue_google", comment: ""), emoji: "G")
                    
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
