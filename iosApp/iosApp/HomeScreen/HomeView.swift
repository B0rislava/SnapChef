//
//  HomeView.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//
import SwiftUI
import PhotosUI

struct HomeView: View {
    var onGenerateRecipes: ([String]) -> Void = { _ in }

    @StateObject private var permissions = CameraPermissionHandler()
    @StateObject private var viewModel   = HomeViewModel()

    @State private var showModal     = false
    @State private var newIngredient = ""

    // Photo pickers
    @State private var selectedPhotoItem: PhotosPickerItem? = nil
    @State private var showPhotoPicker  = false
    @State private var showCameraPicker = false

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color.greenSecondary.opacity(0.45), Color.greenBackground],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 0) {
                Spacer()

                // Hero icon
                ZStack {
                    Circle()
                        .fill(Color.greenPrimary.opacity(0.10))
                        .frame(width: 200, height: 200)
                    Image(systemName: "fork.knife.circle.fill")
                        .font(.system(size: 90))
                        .foregroundColor(Color.greenPrimary.opacity(0.25))
                }

                Spacer().frame(height: 32)

                Text("Snap your ingredients")
                    .font(.system(size: 26, weight: .heavy))
                    .foregroundColor(Color.greenPrimary)
                    .multilineTextAlignment(.center)

                Spacer().frame(height: 12)

                Text("Take a photo or pick from your gallery.\nWe'll recognise what you have and generate delicious recipes instantly.")
                    .font(.system(size: 16))
                    .foregroundColor(Color.greenOnBackground.opacity(0.7))
                    .multilineTextAlignment(.center)

                Spacer().frame(height: 40)

                HStack(spacing: 16) {
                    // Camera button
                    Button(action: onCameraSnap) {
                        VStack(spacing: 10) {
                            Image(systemName: "camera.fill")
                                .font(.system(size: 28, weight: .semibold))
                                .foregroundColor(.white)
                                .frame(width: 60, height: 60)
                                .background(Color.greenPrimary)
                                .clipShape(RoundedRectangle(cornerRadius: 18))

                            Text("Camera")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(Color.greenPrimary)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 20)
                        .background(Color.white)
                        .clipShape(RoundedRectangle(cornerRadius: 24))
                        .shadow(color: Color.greenPrimary.opacity(0.12), radius: 8, x: 0, y: 4)
                    }
                    .buttonStyle(.plain)

                    // Gallery button
                    Button(action: onGalleryPick) {
                        VStack(spacing: 10) {
                            Image(systemName: "photo.on.rectangle.angled")
                                .font(.system(size: 28, weight: .semibold))
                                .foregroundColor(Color.greenPrimary)
                                .frame(width: 60, height: 60)
                                .background(Color.greenSecondary.opacity(0.35))
                                .clipShape(RoundedRectangle(cornerRadius: 18))

                            Text("Gallery")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(Color.greenPrimary)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 20)
                        .background(Color.white)
                        .clipShape(RoundedRectangle(cornerRadius: 24))
                        .shadow(color: Color.greenPrimary.opacity(0.12), radius: 8, x: 0, y: 4)
                    }
                    .buttonStyle(.plain)
                }

                Spacer()
            }
            .padding(.horizontal, 32)
        }

        .photosPicker(
            isPresented: $showPhotoPicker,
            selection: $selectedPhotoItem,
            matching: .images
        )
        .onChange(of: selectedPhotoItem) { _, newItem in
            guard let newItem else { return }
            showPhotoPicker = false
            showModal       = true

            Task {
                if let data = try? await newItem.loadTransferable(type: Data.self),
                   let image = UIImage(data: data) {
                    await MainActor.run {
                        viewModel.analyzeImage(image: image)
                    }
                } else {
                    await MainActor.run {
                        viewModel.errorMessage = "Failed to load the selected image."
                    }
                }
                // Clear selection so the same image can be picked again if needed
                selectedPhotoItem = nil
            }
        }

        // Ingredients sheet
        .sheet(isPresented: $showModal) {
            IngredientsSheet(
                isAnalyzing:   $viewModel.isAnalyzing,
                ingredients:   $viewModel.ingredients,
                newIngredient: $newIngredient,
                onGenerate: {
                    showModal = false
                    onGenerateRecipes(viewModel.ingredients)
                }
            )
            .presentationDetents([.medium, .large])
            .presentationDragIndicator(.visible)
        }
        
        // Camera sheet
        .fullScreenCover(isPresented: $showCameraPicker) {
            ImagePicker(sourceType: .camera) { image in
                showModal = true
                viewModel.analyzeImage(image: image)
            }
            .ignoresSafeArea()
        }
        
        // Error alert
        .alert("Error", isPresented: Binding(
            get: { viewModel.errorMessage != nil },
            set: { if !$0 { viewModel.errorMessage = nil } }
        )) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(viewModel.errorMessage ?? "An unknown error occurred.")
        }

        // Camera denied alert
        .alert("Camera Access Required", isPresented: $permissions.showCameraDeniedAlert) {
            Button("Open Settings") { openSettings() }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("SnapChef needs access to your camera to take photos of ingredients. Please enable Camera access in Settings.")
        }

        // Photos denied alert
        .alert("Photo Library Access Required", isPresented: $permissions.showPhotosDeniedAlert) {
            Button("Open Settings") { openSettings() }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("SnapChef needs access to your photo library to pick ingredient photos. Please enable Photos access in Settings.")
        }
    }

    private func onCameraSnap() {
        permissions.requestCameraPermission {
            showCameraPicker = true
        }
    }

    private func onGalleryPick() {
        permissions.requestPhotosPermission {
            showPhotoPicker = true
        }
    }

    private func openSettings() {
        if let url = URL(string: UIApplication.openSettingsURLString) {
            UIApplication.shared.open(url)
        }
    }
}

private struct IngredientsSheet: View {
    @Binding var isAnalyzing:   Bool
    @Binding var ingredients:   [String]
    @Binding var newIngredient: String
    var onGenerate: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            RoundedRectangle(cornerRadius: 2)
                .fill(Color.greenSecondary)
                .frame(width: 40, height: 4)
                .padding(.top, 8)

            ScrollView(showsIndicators: false) {
                VStack(spacing: 0) {
                    Spacer().frame(height: 16)

                    Text(isAnalyzing ? "Analyzing Photo..." : "Ingredients Found")
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(Color.greenPrimary)

                    Spacer().frame(height: 8)

                    if isAnalyzing {
                        VStack(spacing: 16) {
                            ProgressView()
                                .tint(Color.greenPrimary)
                                .scaleEffect(1.4)
                            Text("Identifying food items with AI...")
                                .font(.system(size: 14))
                                .foregroundColor(Color.greenOnBackground.opacity(0.6))
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 40)
                        .transition(.opacity)

                    } else {
                        // results state
                        VStack(spacing: 16) {
                            Text("Confirm the items below, or remove any mistakes before generating recipes.")
                                .font(.system(size: 14))
                                .foregroundColor(Color.greenOnBackground.opacity(0.6))
                                .multilineTextAlignment(.center)
                                .frame(maxWidth: .infinity)

                            // ingredient list
                            VStack(spacing: 12) {
                                ForEach(ingredients, id: \.self) { item in
                                    HStack {
                                        Image(systemName: "checkmark.circle.fill")
                                            .foregroundColor(Color.greenPrimary)
                                            .font(.system(size: 24))

                                        Text(item)
                                            .font(.system(size: 16, weight: .medium))
                                            .foregroundColor(Color.greenOnBackground)

                                        Spacer()

                                        Button(action: {
                                            withAnimation {
                                                ingredients.removeAll { $0 == item }
                                            }
                                        }) {
                                            Image(systemName: "trash")
                                                .foregroundColor(Color.greenSecondary)
                                                .font(.system(size: 18))
                                        }
                                    }
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 12)
                                    .background(Color.greenBackground)
                                    .clipShape(RoundedRectangle(cornerRadius: 16))
                                }
                            }

                            // add ingredient row
                            HStack(spacing: 12) {
                                AuthTextField(
                                    value: $newIngredient,
                                    placeholder: "Add missing ingredient"
                                )

                                Button(action: addIngredient) {
                                    Image(systemName: "plus")
                                        .foregroundColor(.white)
                                        .font(.system(size: 22, weight: .semibold))
                                        .frame(width: 56, height: 56)
                                        .background(Color.greenPrimary)
                                        .clipShape(RoundedRectangle(cornerRadius: 16))
                                }
                            }

                            // generate button
                            Button(action: onGenerate) {
                                Text("Generate Recipes")
                                    .font(.system(size: 15, weight: .semibold))
                                    .foregroundColor(.white)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 56)
                                    .background(ingredients.isEmpty
                                        ? Color.greenPrimary.opacity(0.4)
                                        : Color.greenPrimary)
                                    .clipShape(Capsule())
                            }
                            .disabled(ingredients.isEmpty)
                            .animation(.easeInOut(duration: 0.2), value: ingredients.isEmpty)
                        }
                        .transition(.opacity)
                    }

                    Spacer().frame(height: 32)
                }
                .padding(.horizontal, 24)
            }
        }
        .animation(.easeInOut(duration: 0.3), value: isAnalyzing)
        .background(Color.white)
    }

    private func addIngredient() {
        let trimmed = newIngredient.trimmingCharacters(in: .whitespaces)
        guard !trimmed.isEmpty else { return }
        withAnimation { ingredients.append(trimmed) }
        newIngredient = ""
    }
}
