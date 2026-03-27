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

                    VStack(alignment: .leading, spacing: 8) {
                        Text("Welcome Back")
                            .font(.system(size: 32, weight: .bold))
                            .foregroundColor(Color.greenOnBackground)
                        Text("Sign in to continue your zero-waste journey")
                            .font(.system(size: 16))
                            .foregroundColor(Color.greenOnBackground.opacity(0.7))
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    
                    Spacer().frame(height: 32)
                    
                    VStack(spacing: 20) {
                        // Email
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Email")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(Color.greenOnBackground)
                            
                            TextField("Enter your email", text: $viewModel.email)
                                .keyboardType(.emailAddress)
                                .autocapitalization(.none)
                                .padding()
                                .background(Color.white)
                                .cornerRadius(12)
                                .shadow(color: .black.opacity(0.05), radius: 4, y: 2)
                        }
                        
                        // Password
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Password")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(Color.greenOnBackground)
                            
                            HStack {
                                if showPass {
                                    TextField("Enter your password", text: $viewModel.password)
                                } else {
                                    SecureField("Enter your password", text: $viewModel.password)
                                }
                                
                                Button(action: { showPass.toggle() }) {
                                    Image(systemName: showPass ? "eye.slash.fill" : "eye.fill")
                                        .foregroundColor(.gray)
                                }
                            }
                            .padding()
                            .background(Color.white)
                            .cornerRadius(12)
                            .shadow(color: .black.opacity(0.05), radius: 4, y: 2)
                        }
                    }
                    
                    if let error = viewModel.errorMessage {
                        Text(error)
                            .foregroundColor(.red)
                            .font(.system(size: 14))
                            .padding(.top, 8)
                    }

                    Spacer().frame(height: 32)

                    Button(action: {
                        viewModel.signIn()
                    }) {
                        HStack {
                            if viewModel.isLoading {
                                ProgressView().progressViewStyle(CircularProgressViewStyle(tint: .white))
                            } else {
                                Text("Sign In")
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
                    
                    Spacer().frame(height: 24)

                    SocialButton(label: "Continue with Google", emoji: "G")
                        .onTapGesture {
                            let rootVC = getRootViewController()
                            viewModel.handleGoogleAuth(presenting: rootVC)
                        }
                        .disabled(viewModel.isLoading)
                        .opacity(viewModel.isLoading ? 0.6 : 1.0)

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
