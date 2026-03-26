//
//  HomeView.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//
import SwiftUI

struct HomeView: View {
    var onGenerateRecipes: ([String]) -> Void = { _ in }

    @StateObject private var permissions = CameraPermissionHandler()

    @State private var showModal     = false
    @State private var isAnalyzing   = false
    @State private var ingredients:  [String] = []
    @State private var newIngredient = ""

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color.greenSecondary.opacity(0.45), Color.greenBackground],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 0) {
                Button(action: onCameraSnap) {
                    ZStack {
                        Circle()
                            .fill(Color.greenPrimary.opacity(0.15))
                            .frame(width: 240, height: 240)

                        Circle()
                            .fill(Color.greenPrimary)
                            .frame(width: 180, height: 180)

                        Image(systemName: "camera.fill")
                            .font(.system(size: 80))
                            .foregroundColor(.white)
                    }
                }
                .buttonStyle(.plain)

                Spacer().frame(height: 40)

                Text("Snap your ingredients")
                    .font(.system(size: 26, weight: .heavy))
                    .foregroundColor(Color.greenPrimary)
                    .multilineTextAlignment(.center)

                Spacer().frame(height: 16)

                Text("Take a photo of the food in your kitchen.\nWe'll recognize what you have and generate delicious recipes instantly.")
                    .font(.system(size: 16))
                    .foregroundColor(Color.greenOnBackground.opacity(0.7))
                    .multilineTextAlignment(.center)
            }
            .padding(.horizontal, 32)
        }
        .sheet(isPresented: $showModal) {
            IngredientsSheet(
                isAnalyzing:   $isAnalyzing,
                ingredients:   $ingredients,
                newIngredient: $newIngredient,
                onGenerate: {
                    showModal = false
                    onGenerateRecipes(ingredients)
                }
            )
            .presentationDetents([.medium, .large])
            .presentationDragIndicator(.visible)
        }
        // Denied alert — directs user to Settings
        .alert("Permission Required", isPresented: $permissions.showDeniedAlert) {
            Button("Open Settings") {
                if let url = URL(string: UIApplication.openSettingsURLString) {
                    UIApplication.shared.open(url)
                }
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("SnapChef needs access to your camera and photo library to identify ingredients. Please enable both in Settings.")
        }
    }

    private func onCameraSnap() {
        permissions.requestPermissions {
            ingredients  = []
            isAnalyzing  = true
            showModal    = true

            // Simulate AI recognition
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                withAnimation(.easeInOut(duration: 0.3)) {
                    ingredients = ["Tomatoes", "Eggs", "Cheese", "Onion"]
                    isAnalyzing = false
                }
            }
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
