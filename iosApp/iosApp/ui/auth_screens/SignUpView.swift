//
//  SignUpView.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//

import SwiftUI
import Shared

struct SignUpView: View {
    var onBack: () -> Void = {}
    var onSignUp: () -> Void = {}
    var onSignIn: () -> Void = {}
    var onVerifyRequired: (String) -> Void = { _ in }

    @StateObject private var viewModel = SignUpViewModel()

    @State private var name = ""
    @State private var email = ""
    @State private var password = ""
    @State private var showPass = false
    @State private var agreeTerms = false

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color.greenBackground, Color.greenSecondary.opacity(0.50)],
                startPoint: .top,
                endPoint: .bottom
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

                    Text("Join SnapChef!")
                        .font(.system(size: 26, weight: .heavy))
                        .foregroundColor(Color.greenPrimary)

                    Spacer().frame(height: 6)

                    Text("Start your journey today - Snap, cook, and enjoy.")
                        .font(.system(size: 14))
                        .foregroundColor(Color.greenOnBackground.opacity(0.55))
                        .multilineTextAlignment(.center)

                    Spacer().frame(height: 32)

                    VStack(spacing: 16) {

                        // name
                        AuthTextField(
                            value: $name,
                            placeholder: "Enter name",
                            leadingIcon: AnyView(
                                Image(systemName: "person")
                                    .foregroundColor(Color.greenPrimary)
                            )
                        )

                        // email
                        AuthTextField(
                            value: $email,
                            placeholder: "Enter your email",
                            leadingIcon: AnyView(
                                Image(systemName: "envelope")
                                    .foregroundColor(Color.greenPrimary)
                            ),
                            keyboardType: .emailAddress
                        )
                        .autocapitalization(.none)
                        .disableAutocorrection(true)

                        // password
                        AuthTextField(
                            value: $password,
                            placeholder: "Create password",
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

                        // terms
                        HStack(spacing: 4) {
                            Toggle("", isOn: $agreeTerms)
                                .toggleStyle(CheckboxToggleStyle())

                            Text("By creating an account you agree to our Terms & Conditions and our Privacy Policy.")
                                .font(.system(size: 12))
                                .foregroundColor(Color.greenOnBackground.opacity(0.65))
                                .padding(.top, 10)



                            Spacer()
                        }
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
                        viewModel.signUp(name: name, email: email, password: password)
                    }) {
                        ZStack {
                            if viewModel.isLoading {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            } else {
                                Text("Sign Up")
                                    .font(.system(size: 15, weight: .semibold))
                            }
                        }
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(agreeTerms ? Color.greenPrimary : Color.greenPrimary.opacity(0.4))
                        .clipShape(Capsule())
                        .shadow(
                            color: agreeTerms ? Color.greenPrimary.opacity(0.4) : .clear,
                            radius: 8, x: 0, y: 4
                        )
                    }
                    .disabled(!agreeTerms || viewModel.isLoading)
                    .animation(.easeInOut(duration: 0.2), value: agreeTerms)
                    .animation(.easeInOut, value: viewModel.isLoading)

                    Spacer().frame(height: 24)

                    OrDivider()

                    Spacer().frame(height: 20)

                    SocialButton(label: "Sign up with Google", emoji: "G")

                    Spacer().frame(height: 24)

                    // sign-in link
                    HStack(spacing: 0) {
                        Text("Already have an account")
                            .font(.system(size: 14))
                            .foregroundColor(Color.greenOnBackground.opacity(0.6))

                        Button(action: onSignIn) {
                            Text("Login")
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
        .onChange(of: viewModel.isSuccess) { oldValue, newValue in
            if newValue {
                onVerifyRequired(email)
            }
        }
    }
}
