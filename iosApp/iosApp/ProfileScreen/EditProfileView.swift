//
//  EditProfileView.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//

import SwiftUI
import PhotosUI

struct EditProfileView: View {
    let userName: String
    let userEmail: String
    let profileImageUri: URL?
    var onPickImage: (URL) -> Void = { _ in }
    var onSave: (String, String) -> Void = { _, _ in }
    var onCancel: () -> Void = {}

    @State private var editedName: String
    @State private var editedEmail: String
    @State private var selectedPhotoItem: PhotosPickerItem? = nil
    @State private var pickedImageURL: URL? = nil
    
    @State private var showPhotoPicker = false
    @StateObject private var permissionHandler = CameraPermissionHandler()

    private var initials: String { editedName.toInitials() }
    private var displayImageUri: URL? { pickedImageURL ?? profileImageUri }

    init(
        userName: String,
        userEmail: String,
        profileImageUri: URL?,
        onPickImage: @escaping (URL) -> Void = { _ in },
        onSave: @escaping (String, String) -> Void = { _, _ in },
        onCancel: @escaping () -> Void = {}
    ) {
        self.userName = userName
        self.userEmail = userEmail
        self.profileImageUri = profileImageUri
        self.onPickImage = onPickImage
        self.onSave = onSave
        self.onCancel = onCancel
        _editedName  = State(initialValue: userName)
        _editedEmail = State(initialValue: userEmail)
    }

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

                    // Back button
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

                    // Title
                    Text("Edit Profile")
                        .font(.system(size: 28, weight: .heavy))
                        .foregroundColor(Color.greenPrimary)

                    Spacer().frame(height: 6)

                    Text("Update your details and photo.")
                        .font(.system(size: 14))
                        .foregroundColor(Color.greenOnBackground.opacity(0.55))

                    Spacer().frame(height: 40)

                    // Avatar with edit button
                    AvatarPickerView(
                        imageUri: displayImageUri,
                        initials: initials,
                        onEditTap: {
                            permissionHandler.requestBothPermissions {
                                showPhotoPicker = true
                            }
                        }
                    )
                    .photosPicker(
                        isPresented: $showPhotoPicker,
                        selection: $selectedPhotoItem,
                        matching: .images
                    )
                    .onChange(of: selectedPhotoItem) { oldValue, newItem in
                        Task {
                            guard let newItem,
                                  let data = try? await newItem.loadTransferable(type: Data.self),
                                  let tmpURL = saveToTemp(data: data)
                            else { return }
                            pickedImageURL = tmpURL
                            onPickImage(tmpURL)
                        }
                    }

                    Spacer().frame(height: 40)

                    // Editable fields card
                    VStack(spacing: 16) {
                        EditProfileTextField(
                            value: $editedName,
                            placeholder: "Full Name",
                            icon: "person",
                            keyboardType: .default
                        )
                        EditProfileTextField(
                            value: $editedEmail,
                            placeholder: "Email Address",
                            icon: "envelope",
                            keyboardType: .emailAddress
                        )
                    }
                    .padding(24)
                    .frame(maxWidth: .infinity)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 24))
                    .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 4)

                    Spacer().frame(height: 32)

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
                                .overlay(
                                    Capsule()
                                        .stroke(Color.greenSecondary, lineWidth: 1.5)
                                )
                        }
                        .buttonStyle(BouncyStyle())

                        // Save — filled
                        Button(action: { onSave(editedName, editedEmail) }) {
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
            }
        }
        .navigationBarHidden(true)
        .alert("Camera Access Required", isPresented: $permissionHandler.showCameraDeniedAlert) {
            Button("Cancel", role: .cancel) {}
            Button("Settings") { openSettings() }
            } message: {
                Text("SnapChef needs camera access to take a profile photo. Please enable it in Settings.")
            }
            // Photos denied alert
        .alert("Photo Library Access Required", isPresented: $permissionHandler.showPhotosDeniedAlert) {
            Button("Cancel", role: .cancel) {}
            Button("Settings") { openSettings() }
            } message: {
                Text("SnapChef needs photo library access to pick a profile picture. Please enable it in Settings.")
            }
    }

    private func saveToTemp(data: Data) -> URL? {
        let url = FileManager.default.temporaryDirectory
            .appendingPathComponent(UUID().uuidString + ".jpg")
        try? data.write(to: url)
        return url
    }
    
    private func openSettings() {
        if let url = URL(string: UIApplication.openSettingsURLString) {
        UIApplication.shared.open(url)
        }
    }
}

private struct AvatarPickerView: View {
    let imageUri: URL?
    let initials: String
    let onEditTap: () -> Void

    var body: some View {
        ZStack(alignment: .bottom) {
            Circle()
                .fill(Color.white)
                .frame(width: 140, height: 140)
                .overlay(
                    ProfilePhoto(imageUri: imageUri, initials: initials)
                        .frame(width: 128, height: 128)
                )

            Button(action: onEditTap) {
                ZStack {
                    Circle()
                        .fill(Color.white)
                        .frame(width: 40, height: 40)

                    Circle()
                        .fill(Color.greenPrimary)
                        .frame(width: 32, height: 32)

                    Image(systemName: "pencil")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(.white)
                }
            }
            .offset(y: 14)
        }
        .padding(.bottom, 14)
    }
}

private struct EditProfileTextField: View {
    @Binding var value: String
    let placeholder: String
    let icon: String
    var keyboardType: UIKeyboardType = .default

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 18, weight: .medium))
                .foregroundColor(Color.greenPrimary)
                .frame(width: 24)

            TextField(placeholder, text: $value)
                .font(.system(size: 16))
                .foregroundColor(Color.greenOnBackground)
                .keyboardType(keyboardType)
                .autocapitalization(keyboardType == .emailAddress ? .none : .words)
                .disableAutocorrection(keyboardType == .emailAddress)
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
}

private struct BouncyStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
            .animation(.spring(response: 0.25, dampingFraction: 0.65),
                       value: configuration.isPressed)
    }
}

private extension String {
    func toInitials() -> String {
        let parts = trimmingCharacters(in: .whitespaces)
            .components(separatedBy: .whitespaces)
            .filter { !$0.isEmpty }
        guard !parts.isEmpty else { return "JD" }
        if parts.count == 1 { return String(parts[0].prefix(2)).uppercased() }
        return (String(parts[0].first!) + String(parts[parts.count - 1].first!)).uppercased()
    }
}
