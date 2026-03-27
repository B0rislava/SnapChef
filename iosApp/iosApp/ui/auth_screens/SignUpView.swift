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
                    
                    Spacer().frame(height: 32)
                    
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Create Account")
                            .font(.system(size: 32, weight: .bold))
                            .foregroundColor(Color.greenOnBackground)
                        Text("Join us to start reducing food waste")
                            .font(.system(size: 16))
                            .foregroundColor(Color.greenOnBackground.opacity(0.7))
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    
                    Spacer().frame(height: 32)
                    
                    VStack(spacing: 20) {
                        // Name
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Full Name")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(Color.greenOnBackground)
                            
                            TextField("Enter your name", text: $name)
                                .padding()
                                .background(Color.white)
                                .cornerRadius(12)
                                .shadow(color: .black.opacity(0.05), radius: 4, y: 2)
                        }

                        // Email
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Email")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(Color.greenOnBackground)
                            
                            TextField("Enter your email", text: $email)
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
                                    TextField("Create a password", text: $password)
                                } else {
                                    SecureField("Create a password", text: $password)
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
                    
                    Toggle(isOn: $agreeTerms) {
                        Text("I agree to the Terms of Service and Privacy Policy")
                            .font(.system(size: 12))
                            .foregroundColor(Color.greenOnBackground.opacity(0.7))
                    }
                    .toggleStyle(CheckboxToggleStyle())
                    .padding(.top, 16)
                    
                    if let error = viewModel.errorMessage {
                        Text(error)
                            .foregroundColor(.red)
                            .font(.system(size: 14))
                            .padding(.top, 8)
                    }

                    Spacer().frame(height: 32)

                    Button(action: {
                        viewModel.signUp(name: name, email: email, password: password)
                    }) {
                        HStack {
                            if viewModel.isLoading {
                                ProgressView().progressViewStyle(CircularProgressViewStyle(tint: .white))
                            } else {
                                Text("Sign Up")
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
                    .disabled(!agreeTerms || viewModel.isLoading)
                    .animation(.easeInOut(duration: 0.2), value: agreeTerms)
                    .animation(.easeInOut, value: viewModel.isLoading)

                    Spacer().frame(height: 24)

                    SocialButton(label: "Sign up with Google", emoji: "G")
                        .onTapGesture {
                            let rootVC = getRootViewController()
                            viewModel.handleGoogleAuth(presenting: rootVC)
                        }
                        .disabled(viewModel.isLoading)
                        .opacity(viewModel.isLoading ? 0.6 : 1.0)

                    Spacer().frame(height: 24)

                    // sign-in link
                    HStack(spacing: 0) {
                        Text("Already have an account? ")
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
        .onChange(of: viewModel.isSuccess) { _, newValue in
            if newValue {
                if viewModel.loggedInDirectly {
                    onSignUp()
                } else {
                    onVerifyRequired(email)
                }
            }
        }
    }
}
