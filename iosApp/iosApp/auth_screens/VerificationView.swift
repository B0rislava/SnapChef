//
//  VerificationView.swift
//  iosApp
//
//  Created by gergana on 3/28/26.
//

import SwiftUI

struct VerificationView: View {
    @ObservedObject var viewModel: VerificationViewModel

    let email:     String
    var onBack:    () -> Void = {}
    var onSuccess: () -> Void = {}

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color.greenSecondary.opacity(0.55), Color.greenBackground],
                startPoint: .top, endPoint: .bottom
            )
            .ignoresSafeArea()

            Circle()
                .fill(Color.greenPrimary.opacity(0.10))
                .frame(width: 220, height: 220)
                .offset(x: 140, y: -50)

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

                    Text("Verification")
                        .font(.system(size: 26, weight: .heavy))
                        .foregroundColor(Color.greenPrimary)

                    Spacer().frame(height: 6)

                    Text("We sent a 6-digit code to:\n\(email)")
                        .font(.system(size: 14))
                        .foregroundColor(Color.greenOnBackground.opacity(0.55))
                        .multilineTextAlignment(.center)

                    Spacer().frame(height: 40)

                    VStack(spacing: 24) {

                        AuthTextField(
                            value: Binding(
                                get: { viewModel.code },
                                set: { viewModel.updateCode($0) }
                            ),
                            placeholder: "Enter 6-digit code",
                            leadingIcon: AnyView(
                                Image(systemName: "envelope.badge")
                                    .foregroundColor(Color.greenPrimary)
                            ),
                            keyboardType: .numberPad
                        )

                        // Error
                        if let error = viewModel.errorMessage {
                            Text(error)
                                .font(.system(size: 13))
                                .foregroundColor(.red)
                                .multilineTextAlignment(.center)
                        }

                        // Verify button
                        Button(action: {
                            viewModel.verify(email: email, onSuccess: onSuccess)
                        }) {
                            Group {
                                if viewModel.isLoading {
                                    ProgressView().tint(.white)
                                } else {
                                    Text("Verify & Continue")
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
                    }
                    .padding(24)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 24))
                    .shadow(color: .black.opacity(0.08), radius: 12, x: 0, y: 4)

                    Spacer().frame(height: 32)
                }
                .padding(.horizontal, 24)
            }
        }
        .navigationBarHidden(true)
    }
}
