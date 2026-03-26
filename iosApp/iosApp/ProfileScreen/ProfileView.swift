//
//  ProfileView.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//

import SwiftUI

private struct SavedRecipe: Identifiable {
    let id = UUID()
    let title: String
    let isQuick: Bool
}

struct ProfileView: View {
    @State private var userName: String = "John Doe"
    @State private var userEmail: String = "john.doe@example.com"
    @State private var profileImageUri: URL? = nil
    
    @State private var isEditingProfile: Bool = false

    var onBack: () -> Void = {}
    var onLogout: () -> Void = {}
    var onDeleteAccount: () -> Void = {}

    private var initials: String { userName.toInitials() }

    private let recipes: [SavedRecipe] = [
        SavedRecipe(title: "Omelette with Cheese",     isQuick: true),
        SavedRecipe(title: "Tomato Egg Fried Rice",    isQuick: true),
        SavedRecipe(title: "Baked Veggie Pasta",       isQuick: false),
        SavedRecipe(title: "Leftover Chicken Wraps",   isQuick: true),
    ]

    var body: some View {
        Group {
            if isEditingProfile {
                EditProfileView(
                    userName: userName,
                    userEmail: userEmail,
                    profileImageUri: profileImageUri,
                    onPickImage: { newImageURL in
                        self.profileImageUri = newImageURL
                    },
                    onSave: { updatedName, updatedEmail in
                        self.userName = updatedName
                        self.userEmail = updatedEmail
                        withAnimation {
                            self.isEditingProfile = false
                        }
                    },
                    onCancel: {
                        withAnimation {
                            self.isEditingProfile = false
                        }
                    }
                )
            } else {
                displayProfileContent
            }
        }
    }
    
    private var displayProfileContent: some View {
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
                    Spacer().frame(height: 24)

                    HStack(spacing: 10) {
                        Button(action: onBack) {
                            Image(systemName: "arrow.left")
                                .font(.system(size: 18, weight: .semibold))
                                .foregroundColor(Color.greenPrimary)
                                .frame(width: 40, height: 40)
                                .background(Color.greenPrimary.opacity(0.10))
                                .clipShape(Circle())
                        }

                        Text("Your Profile")
                            .font(.system(size: 28, weight: .heavy))
                            .foregroundColor(Color.greenPrimary)

                        Spacer()
                    }
                    .frame(maxWidth: .infinity)

                    Spacer().frame(height: 32)

                    // Avatar
                    AvatarView(imageUri: profileImageUri, initials: initials)

                    Spacer().frame(height: 32)

                    // Profile fields card
                    VStack(spacing: 16) {
                        ReadOnlyProfileField(label: "Name",  value: userName,  icon: "person")
                        ReadOnlyProfileField(label: "Email", value: userEmail, icon: "envelope")
                    }
                    .padding(24)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 24))
                    .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 4)

                    Spacer().frame(height: 24)

                    // Saved recipes card
                    SavedRecipesCard(recipes: recipes)

                    Spacer().frame(height: 32)

                    // Bottom action buttons
                    HStack(spacing: 16) {
                        BouncyActionButton(
                            text: "Logout",
                            container: Color.greenPrimary,
                            content: .white,
                            action: onLogout
                        )
                        BouncyActionButton(
                            text: "Delete",
                            container: Color.greenSecondary.opacity(0.5),
                            content: Color.greenPrimary,
                            action: onDeleteAccount
                        )
                        BouncyActionButton(
                            text: "Edit",
                            container: Color.greenSecondary,
                            content: Color.greenOnBackground,
                            action: {
                                // Transition into Edit Mode
                                withAnimation {
                                    isEditingProfile = true
                                }
                            }
                        )
                    }

                    Spacer().frame(height: 32)
                }
                .padding(.horizontal, 24)
            }
        }
        .navigationBarHidden(true)
    }
}

private struct AvatarView: View {
    let imageUri: URL?
    let initials: String

    var body: some View {
        Circle()
            .fill(Color.white)
            .frame(width: 140, height: 140)
            .overlay(
                ProfilePhoto(imageUri: imageUri, initials: initials)
                    .frame(width: 128, height: 128)
            )
    }
}

private struct ReadOnlyProfileField: View {
    let label: String
    let value: String
    let icon: String

    var body: some View {
        HStack(spacing: 16) {
            ZStack {
                Circle()
                    .fill(Color.greenSecondary.opacity(0.35))
                    .frame(width: 48, height: 48)
                Image(systemName: icon)
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(Color.greenPrimary)
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(Color.greenOnBackground.opacity(0.55))
                    .textCase(.uppercase)
                    .tracking(0.5)

                Text(value)
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(Color.greenOnBackground)
            }

            Spacer()
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private struct SavedRecipesCard: View {
    let recipes: [SavedRecipe]
    @State private var currentPage = 0

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("Saved recipes")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(Color.greenPrimary)

            Spacer().frame(height: 16)

            TabView(selection: $currentPage) {
                ForEach(Array(recipes.enumerated()), id: \.element.id) { index, recipe in
                    RecipeCard(recipe: recipe)
                        .tag(index)
                        .padding(.horizontal, 2)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
            .frame(height: 110)

            Spacer().frame(height: 12)

            HStack(spacing: 8) {
                Spacer()
                ForEach(0..<recipes.count, id: \.self) { index in
                    Circle()
                        .fill(
                            index == currentPage
                                ? Color.greenPrimary
                                : Color.greenSecondary.opacity(0.6)
                        )
                        .frame(
                            width:  index == currentPage ? 9 : 7,
                            height: index == currentPage ? 9 : 7
                        )
                        .animation(.easeInOut(duration: 0.2), value: currentPage)
                }
                Spacer()
            }
        }
        .padding(24)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 24))
        .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 4)
    }
}

private struct RecipeCard: View {
    let recipe: SavedRecipe
    @State private var isPressed = false

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            PillView(text: recipe.isQuick ? "Quick" : "Standard", isQuick: recipe.isQuick)

            Text(recipe.title)
                .font(.system(size: 16, weight: .semibold))
                .foregroundColor(Color.greenOnBackground)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.greenBackground)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color.greenSecondary, lineWidth: 1.5)
        )
        .scaleEffect(isPressed ? 0.97 : 1.0)
        .animation(.spring(response: 0.25, dampingFraction: 0.65), value: isPressed)
        .onLongPressGesture(
            minimumDuration: .infinity,
            pressing: { pressing in isPressed = pressing },
            perform: {}
        )
    }
}

private struct PillView: View {
    let text: String
    let isQuick: Bool

    var body: some View {
        Text(text)
            .font(.system(size: 13, weight: .semibold))
            .foregroundColor(isQuick ? .white : Color.greenOnBackground)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(isQuick ? Color.greenPrimary : Color.greenSecondary)
            .clipShape(Capsule())
    }
}

private struct BouncyActionButton: View {
    let text: String
    let container: Color
    let content: Color
    let action: () -> Void

    @State private var isPressed = false

    var body: some View {
        Button(action: action) {
            Text(text)
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(content)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(container)
                .clipShape(RoundedRectangle(cornerRadius: 24))
        }
        .buttonStyle(BouncyButtonStyle())
    }
}

private struct BouncyButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
            .animation(.spring(response: 0.25, dampingFraction: 0.65), value: configuration.isPressed)
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
