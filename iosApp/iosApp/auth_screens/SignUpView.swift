//
//  SignUpView.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//

import SwiftUI

struct SignUpView: View {
    @ObservedObject var viewModel: SignUpViewModel

    var onBack:           () -> Void = {}
    var onSignUp:         () -> Void = {}
    var onSignIn:         () -> Void = {}
    var onVerifyRequired: (String) -> Void = { _ in }

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color.greenBackground, Color.greenSecondary.opacity(0.50)],
                startPoint: .top, endPoint: .bottom
            )
            .ignoresSafeArea()

            Circle()
                .fill(Color.greenPrimary.opacity(0.07))
                .frame(width: 180, height: 180)
                .offset(x: -120, y: 120)

            Circle()
                .fill(Color.greenSecondary.opacity(0.45))
                .frame(width: 120, height: 120)
                .offset(x: 140, y: -260)

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

                    Spacer().frame(height: 28)

                    Text(NSLocalizedString("create_account", comment: ""))
                        .font(.system(size: 26, weight: .heavy))
                        .foregroundColor(Color.greenPrimary)

                    Spacer().frame(height: 6)

                    Text(NSLocalizedString("join", comment: ""))
                        .font(.system(size: 14))
                        .foregroundColor(Color.greenOnBackground.opacity(0.55))
                        .multilineTextAlignment(.center)

                    Spacer().frame(height: 32)

                    VStack(spacing: 16) {

                        AuthTextField(
                            value: Binding(
                                get: { viewModel.name },
                                set: { viewModel.updateName($0) }
                            ),
                            placeholder: NSLocalizedString("enter_name", comment: ""),
                            leadingIcon: AnyView(
                                Image(systemName: "person").foregroundColor(Color.greenPrimary)
                            )
                        )

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
                            placeholder: NSLocalizedString("create_password", comment: ""),
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

                        HStack(spacing: 4) {
                            Toggle("", isOn: Binding(
                                get: { viewModel.agreeTerms },
                                set: { viewModel.setAgreeTerms($0) }
                            ))
                            .toggleStyle(CheckboxToggleStyle())

                            Text(NSLocalizedString("i_agree", comment: ""))
                                .font(.system(size: 14))
                                .foregroundColor(Color.greenOnBackground.opacity(0.65))

                            Button(action: {}) {
                                Text(NSLocalizedString("terms_privacy", comment: ""))
                                    .font(.system(size: 14, weight: .semibold))
                                    .foregroundColor(Color.greenPrimary)
                            }

                            Spacer()
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
                        viewModel.signUp(
                            onVerifyRequired: onVerifyRequired,
                            onSuccess:        onSignUp
                        )
                    }) {
                        Group {
                            if viewModel.isLoading {
                                ProgressView().tint(.white)
                            } else {
                                Text(NSLocalizedString("sign_up", comment: ""))
                                    .font(.system(size: 15, weight: .semibold))
                                    .foregroundColor(.white)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(
                            viewModel.agreeTerms ? Color.greenPrimary : Color.greenPrimary.opacity(0.4)
                        )
                        .clipShape(Capsule())
                        .shadow(
                            color: viewModel.agreeTerms ? Color.greenPrimary.opacity(0.4) : .clear,
                            radius: 8, x: 0, y: 4
                        )
                    }
                    .disabled(!viewModel.agreeTerms || viewModel.isLoading)
                    .animation(.easeInOut(duration: 0.2), value: viewModel.agreeTerms)

                    Spacer().frame(height: 24)

                    OrDivider()

                    Spacer().frame(height: 20)

                    SocialButton(
                        label: NSLocalizedString("continue_google", comment: ""),
                        emoji: "G",
                        action: {
                            Task { @MainActor in
                                let token = await GoogleAuthHelper.signInWithGoogle(context: UIApplication.shared)
                                if let token {
                                    viewModel.googleSignIn(idToken: token, onSuccess: onSignUp)
                                }
                            }
                        }
                    )

                    Spacer().frame(height: 24)

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
                .padding(.horizontal, 24)
            }
        }
        .navigationBarHidden(true)
    }
}
