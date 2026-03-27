
import SwiftUI

struct VerificationView: View {
    let email: String
    var onBack: () -> Void = {}
    var onSuccess: () -> Void = {}
    
    @StateObject private var viewModel = VerificationViewModel()
    
    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color.greenSecondary.opacity(0.55), Color.greenBackground],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()
            
            Circle()
                .fill(Color.greenPrimary.opacity(0.10))
                .frame(width: 220, height: 220)
                .offset(x: 220, y: -50)
            
            VStack {
                HStack {
                    Button(action: onBack) {
                        Image(systemName: "arrow.left")
                            .font(.system(size: 20, weight: .semibold))
                            .foregroundColor(Color.greenPrimary)
                            .frame(width: 40, height: 40)
                            .background(Color.greenPrimary.opacity(0.10))
                            .clipShape(Circle())
                    }
                    .padding(.leading, 24)
                    
                    Spacer()
                }
                .padding(.top, 16)
                
                ScrollView {
                    VStack(alignment: .center, spacing: 0) {
                        Spacer().frame(height: 36)
                        
                        Text("Verification")
                            .font(.system(size: 28, weight: .heavy))
                            .foregroundColor(Color.greenPrimary)
                        
                        Spacer().frame(height: 6)
                        
                        Text("We sent a 6-digit code to:\n\(email)")
                            .font(.system(size: 15))
                            .foregroundColor(Color.greenOnBackground.opacity(0.55))
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 40)
                        
                        Spacer().frame(height: 40)
                        
                        VStack(spacing: 24) {
                            TextField("Enter 6-digit code", text: Binding(
                                get: { viewModel.code },
                                set: { viewModel.updateCode($0) }
                            ))
                            .keyboardType(.numberPad)
                            .padding()
                            .background(Color.greenSecondary.opacity(0.1))
                            .cornerRadius(12)
                            
                            if let errorMessage = viewModel.errorMessage {
                                Text(errorMessage)
                                    .font(.system(size: 14))
                                    .foregroundColor(.red)
                                    .multilineTextAlignment(.center)
                            }
                            
                            Button(action: {
                                viewModel.verify(email: email)
                            }) {
                                HStack {
                                    if viewModel.isLoading {
                                        ProgressView()
                                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                    } else {
                                        Text("Verify & Continue")
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
                        }
                        .padding(24)
                        .background(Color.white)
                        .cornerRadius(24)
                        .shadow(color: .black.opacity(0.05), radius: 8, x: 0, y: 4)
                        .padding(.horizontal, 24)
                        
                        Spacer().frame(height: 40)
                    }
                }
            }
        }
        .onAppear {
            viewModel.onBack = onBack
            viewModel.onSuccess = onSuccess
        }
    }
}
