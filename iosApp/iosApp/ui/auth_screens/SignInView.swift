//
//  SignInView.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//
import SwiftUI
import Shared

struct SignInView: View {
    var onBack:   () -> Void = {}
    var onSignIn: () -> Void = {}
    var onSignUp: () -> Void = {}
    var onVerifyRequired: (String) -> Void = { _ in }

    @StateObject private var viewModel = SignInViewModel()

    @State private var showPass = false

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
                        Button(action: {
                            viewModel.navigateBack()
                        }) {
                            Image(systemName: "arrow.left")
                                .foregroundColor(Color.greenPrimary)
                                .frame(width: 40, height: 40)
                                .background(Color.greenPrimary.opacity(0.10))
                                .clipShape(Circle())
                        }
                        Spacer()
                    }
                    .padding(.top, 16)

                    Spacer().frame(height: 32)

                    Text("Welcome back!")
                        .font(.system(size: 28, weight: .heavy))
                        .foregroundColor(Color.greenPrimary)

                    Spacer().frame(height: 8)

                    Text("Sign in to continue your culinary journey.")
                        .font(.system(size: 15))
                        .foregroundColor(Color.greenOnBackground.opacity(0.6))
                        .multilineTextAlignment(.center)

                    Spacer().frame(height: 36)

                    VStack(spacing: 16) {
                        AuthTextField(
                            value: $viewModel.email,
                            placeholder: "Enter your email",
                            leadingIcon: AnyView(
                                Image(systemName: "envelope")
                                    .foregroundColor(Color.greenPrimary)
                            ),
                            keyboardType: .emailAddress
                        )
                        .autocapitalization(.none)
                        .disableAutocorrection(true)
                        AuthTextField(
                            value: $viewModel.password,
                            placeholder: "Enter password",
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

                        HStack {
                            Toggle("", isOn: $viewModel.rememberMe)
                                .toggleStyle(CheckboxToggleStyle())

                            Text("Remember me")
                                .font(.system(size: 14))
                                .foregroundColor(Color.greenOnBackground.opacity(0.7))

                            Spacer()

                            Button(action: { /* todo */ }) {
                                Text("Forgot Password?")
                                    .font(.system(size: 14, weight: .semibold))
                                    .foregroundColor(Color.greenPrimary)
                            }
                        }
                        .padding(.top, 4)
                    }
                    .padding(24)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 24))
                    .shadow(color: .black.opacity(0.08), radius: 12, x: 0, y: 4)

                    Spacer().frame(height: 24)

                    if let errorMessage = viewModel.errorMessage {
                        Text(errorMessage)
                            .foregroundColor(.red)
                            .font(.system(size: 14))
                            .multilineTextAlignment(.center)
                            .padding(.horizontal)
                        
                        Spacer().frame(height: 12)
                    }

                    Button(action: {
                        viewModel.signIn()
                    }) {
                        ZStack {
                            if viewModel.isLoading {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            } else {
                                Text(NSLocalizedString("login", comment: ""))
                                    .font(.system(size: 16, weight: .bold))
                            }
                        }
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(Color.greenPrimary)
                        .clipShape(Capsule())
                        .shadow(color: Color.greenPrimary.opacity(0.3), radius: 8, x: 0, y: 4)
                    }
                    .disabled(viewModel.isLoading)
                    .animation(.easeInOut, value: viewModel.isLoading)

                    Spacer().frame(height: 24)

                    OrDivider()

                    Spacer().frame(height: 20)

                    SocialButton(label: "Continue with Google",   emoji: "G")

                    Spacer().frame(height: 24)

                    HStack(spacing: 0) {
                        Text("Don't have an account? ")
                            .font(.system(size: 14))
                            .foregroundColor(Color.greenOnBackground.opacity(0.6))

                        Button(action: {
                            viewModel.navigateToSignUp()
                        }) {
                            Text("Create an account")
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
        .onAppear {
            viewModel.onBack = onBack
            viewModel.onSignInSuccess = onSignIn
            viewModel.onNavigateToSignUp = onSignUp
            viewModel.onVerifyRequired = onVerifyRequired
        }
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
