//
//  EditProfileView.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//

import SwiftUI
import Combine

struct EditProfileView: View {

    let userName: String
    let userEmail: String
    let profileImageUri: URL?

    var onSave: (String, String, String, String) -> Void = { _, _, _, _ in }
    var onCancel: () -> Void = {}

    @StateObject private var viewModel = EditProfileViewModel()
    @StateObject private var keyboardResponder = KeyboardResponder()
    
    enum Field: Hashable {
        case name, email, password, confirmPassword
    }
    
    @FocusState private var focusedField: Field?

    var body: some View {
        ZStack(alignment: .topLeading) {

            LinearGradient(
                colors: [Color.greenSecondary.opacity(0.55), Color.greenBackground],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            Circle()
                .fill(Color.greenPrimary.opacity(0.10))
                .frame(width: 240, height: 240)
                .offset(x: UIScreen.main.bounds.width - 60, y: -40)

            ScrollView(showsIndicators: false) {
                VStack(alignment: .center, spacing: 0) {
                    Spacer().frame(height: 16)

                    HStack {
                        Button(action: onCancel) {
                            Image(systemName: "arrow.left")
                                .font(.system(size: 18, weight: .semibold))
                                .foregroundColor(Color.greenPrimary)
                                .frame(width: 40, height: 40)
                                .background(Color.greenPrimary.opacity(0.10))
                                .clipShape(Circle())
                        }
                        Spacer()
                    }

                    Spacer().frame(height: 24)

                    Text("Edit Profile")
                        .font(.system(size: 28, weight: .heavy))
                        .foregroundColor(Color.greenPrimary)

                    Spacer().frame(height: 6)

                    Text("Update your details.")
                        .font(.system(size: 14))
                        .foregroundColor(Color.greenOnBackground.opacity(0.55))

                    Spacer().frame(height: 40)

                    AvatarPickerView(
                        imageUri: profileImageUri,
                        initials: viewModel.uiState.initials
                    )

                    Spacer().frame(height: 40)

                    VStack(spacing: 16) {

                        // Full Name
                        EditProfileTextField(
                            value: Binding(
                                get: { viewModel.uiState.editedName },
                                set: { viewModel.updateName($0) }
                            ),
                            placeholder: "Full Name",
                            icon: "person",
                            keyboardType: .default,
                            field: .name,
                            focusedField: $focusedField
                        )

                        // Email Address
                        EditProfileTextField(
                            value: Binding(
                                get: { viewModel.uiState.editedEmail },
                                set: { viewModel.updateEmail($0) }
                            ),
                            placeholder: "Email Address",
                            icon: "envelope",
                            keyboardType: .emailAddress,
                            field: .email,
                            focusedField: $focusedField
                        )

                        // New Password
                        EditProfileTextField(
                            value: Binding(
                                get: { viewModel.uiState.editedPassword },
                                set: { viewModel.updatePassword($0) }
                            ),
                            placeholder: "New Password (Optional)",
                            icon: "lock",
                            isSecure: true,
                            field: .password,
                            focusedField: $focusedField
                        )

                        // Confirm Password
                        EditProfileTextField(
                            value: Binding(
                                get: { viewModel.uiState.editedConfirmPassword },
                                set: { viewModel.updateConfirmPassword($0) }
                            ),
                            placeholder: "Confirm Password",
                            icon: "lock",
                            isSecure: true,
                            field: .confirmPassword,
                            focusedField: $focusedField
                        )
                    }
                    .padding(24)
                    .frame(maxWidth: .infinity)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 24))
                    .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 4)

                    if let error = viewModel.uiState.errorMessage {
                        Spacer().frame(height: 8)
                        Text(error)
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(Color(UIColor.systemRed))
                            .padding(.horizontal, 14)
                            .padding(.vertical, 10)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(Color(UIColor.systemRed).opacity(0.10))
                            .clipShape(RoundedRectangle(cornerRadius: 14))
                    }

                    Spacer().frame(height: 24)

                    // Cancel / Save buttons
                    HStack(spacing: 16) {

                        Button(action: onCancel) {
                            Text("Cancel")
                                .font(.system(size: 15, weight: .semibold))
                                .foregroundColor(Color.greenOnBackground)
                                .frame(maxWidth: .infinity)
                                .frame(height: 56)
                                .background(Color.white)
                                .clipShape(Capsule())
                                .overlay(Capsule().stroke(Color.greenSecondary, lineWidth: 1.5))
                        }
                        .buttonStyle(BouncyStyle())

                        Button {
                            viewModel.validateAndSave { name, email, password, confirm in
                                onSave(name, email, password, confirm)
                            }
                        } label: {
                            Text("Save")
                                .font(.system(size: 15, weight: .semibold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .frame(height: 56)
                                .background(Color.greenPrimary)
                                .clipShape(Capsule())
                                .shadow(color: Color.greenPrimary.opacity(0.35),
                                        radius: 6, x: 0, y: 4)
                        }
                        .buttonStyle(BouncyStyle())
                    }

                    Spacer().frame(height: 32)
                }
                .padding(.horizontal, 24)
                .frame(maxWidth: .infinity)
            }
            .safeAreaInset(edge: .bottom) { Color.clear.frame(height: 76) }
            .contentShape(Rectangle())
            .onTapGesture {
                focusedField = nil
            }
        }
        .modifier(AdaptiveKeyboardModifier(focusedField: focusedField))
        .animation(.easeOut(duration: 0.3), value: keyboardResponder.currentHeight)
        .navigationBarHidden(true)
        .onAppear {
            viewModel.setInitialValues(name: userName, email: userEmail)
        }
        .onChange(of: userName)  { _, v in viewModel.setInitialValues(name: v, email: userEmail) }
        .onChange(of: userEmail) { _, v in viewModel.setInitialValues(name: userName, email: v) }
    }
}

class KeyboardResponder: ObservableObject {
    @Published var currentHeight: CGFloat = 0
    private var cancellables = Set<AnyCancellable>()
    
    init() {
        let willShow = NotificationCenter.default.publisher(for: UIResponder.keyboardWillShowNotification)
        let willHide = NotificationCenter.default.publisher(for: UIResponder.keyboardWillHideNotification)
        
        willShow
            .compactMap { notification -> CGFloat? in
                guard let keyboardFrame = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? CGRect else {
                    return nil
                }
                return keyboardFrame.height
            }
            .assign(to: \.currentHeight, on: self)
            .store(in: &cancellables)
        
        willHide
            .map { _ in CGFloat(0) }
            .assign(to: \.currentHeight, on: self)
            .store(in: &cancellables)
    }
}

struct AdaptiveKeyboardModifier: ViewModifier {
    @StateObject private var keyboard = KeyboardResponder()
    let focusedField: EditProfileView.Field?
    
    func body(content: Content) -> some View {
        content
            .offset(y: calculateOffset())
            .animation(.easeOut(duration: 0.25), value: keyboard.currentHeight)
    }
    
    private func calculateOffset() -> CGFloat {
        guard keyboard.currentHeight > 0 else { return 0 }
        
        switch focusedField {
        case .name:
            return -keyboard.currentHeight * 0.1
        case .email:
            return -keyboard.currentHeight * 0.2
        case .password:
            return -keyboard.currentHeight * 0.4
        case .confirmPassword:
            return -keyboard.currentHeight * 0.5
        case .none:
            return 0
        }
    }
}

private struct AvatarPickerView: View {
    let imageUri: URL?
    let initials: String

    var body: some View {
        ZStack(alignment: .bottom) {
            Circle()
                .fill(Color.white)
                .frame(width: 140, height: 140)
                .overlay(
                    ProfilePhoto(imageUri: imageUri, initials: initials)
                        .frame(width: 128, height: 128)
                )
        }
        .padding(.bottom, 14)
    }
}


private struct EditProfileTextField: View {
    @Binding var value: String
    let placeholder: String
    let icon: String
    var keyboardType: UIKeyboardType = .default
    var isSecure: Bool = false
    let field: EditProfileView.Field
    @FocusState.Binding var focusedField: EditProfileView.Field?
    
    @State private var isPasswordVisible: Bool = false
    
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 18, weight: .medium))
                .foregroundColor(Color.greenPrimary)
                .frame(width: 24)
            
            Group {
                if isSecure && !isPasswordVisible {
                    SecureField(
                        text: $value,
                        prompt: Text(placeholder).foregroundColor(Color.greenPrimary.opacity(0.6))
                    ) {
                        Text(placeholder)
                    }
                    .focused($focusedField, equals: field)
                    .textContentType(.newPassword)
                } else {
                    TextField(
                        text: $value,
                        prompt: Text(placeholder).foregroundColor(Color.greenPrimary.opacity(0.6))
                    ) {
                        Text(placeholder)
                    }
                    .keyboardType(keyboardType)
                    .autocapitalization(keyboardType == .emailAddress ? .none : .words)
                    .disableAutocorrection(keyboardType == .emailAddress)
                    .focused($focusedField, equals: field)
                    .textContentType(keyboardType == .emailAddress ? .emailAddress : .name)
                }
            }
            .font(.system(size: 16, weight: .medium))
            .foregroundColor(Color.greenPrimary)
            .submitLabel(getSubmitLabel())
            .onSubmit {
                moveToNextField()
            }
            
            if isSecure {
                Button(action: { isPasswordVisible.toggle() }) {
                    Image(systemName: isPasswordVisible ? "eye.slash" : "eye")
                        .font(.system(size: 16))
                        .foregroundColor(Color.greenPrimary)
                }
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 16)
        .background(Color.greenBackground)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color.greenSecondary.opacity(0.6), lineWidth: 1)
        )
    }
    
    private func getSubmitLabel() -> SubmitLabel {
        switch field {
        case .name: return .next
        case .email: return .next
        case .password: return .next
        case .confirmPassword: return .done
        }
    }
    
    private func moveToNextField() {
        switch field {
        case .name:
            focusedField = .email
        case .email:
            focusedField = .password
        case .password:
            focusedField = .confirmPassword
        case .confirmPassword:
            focusedField = nil
        }
    }
}


private struct BouncyStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
            .animation(.spring(response: 0.25, dampingFraction: 0.65),
                       value: configuration.isPressed)
    }
}
