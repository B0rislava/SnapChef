//
//  ProfileView.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//

import SwiftUI

private struct IngredientItem: Identifiable {
    let id = UUID()
    let name: String
    let quantity: String
    let category: IngredientCategory
}

private enum IngredientCategory: String, CaseIterable {
    case produce = "Produce"
    case dairy = "Dairy"
    case protein = "Protein"
    case pantry = "Pantry"

    var icon: String {
        switch self {
        case .produce:  return "leaf"
        case .dairy:    return "drop"
        case .protein:  return "flame"
        case .pantry:   return "archivebox"
        }
    }
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

    private let ingredients: [IngredientItem] = [
        IngredientItem(name: "Eggs",          quantity: "6",       category: .protein),
        IngredientItem(name: "Cheddar Cheese", quantity: "200 g",  category: .dairy),
        IngredientItem(name: "Tomatoes",       quantity: "3",       category: .produce),
        IngredientItem(name: "Chicken Breast", quantity: "400 g",  category: .protein),
        IngredientItem(name: "Pasta",          quantity: "500 g",  category: .pantry),
        IngredientItem(name: "Spinach",        quantity: "100 g",  category: .produce),
        IngredientItem(name: "Milk",           quantity: "1 L",    category: .dairy),
        IngredientItem(name: "Olive Oil",      quantity: "1 bottle", category: .pantry),
    ]

    var body: some View {
        Group {
            if isEditingProfile {
                EditProfileView(
                    userName: userName,
                    userEmail: userEmail,
                    profileImageUri: profileImageUri,
                    onPickImage: { self.profileImageUri = $0 },
                    onSave: { updatedName, updatedEmail in
                        self.userName = updatedName
                        self.userEmail = updatedEmail
                        withAnimation { self.isEditingProfile = false }
                    },
                    onCancel: {
                        withAnimation { self.isEditingProfile = false }
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
                        Text("Your Profile")
                            .font(.system(size: 28, weight: .heavy))
                            .foregroundColor(Color.greenPrimary)

                        Spacer()
                    }

                    Spacer().frame(height: 32)

                    AvatarView(imageUri: profileImageUri, initials: initials)

                    Spacer().frame(height: 32)

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

                    // Ingredient inventory card
                    IngredientInventoryCard(ingredients: ingredients)

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
                            action: { withAnimation { isEditingProfile = true } }
                        )
                    }

                    Spacer().frame(height: 32)
                }
                .padding(.horizontal, 24)
                .safeAreaInset(edge: .bottom) {
                    Color.clear.frame(height: 72)
                }
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

// Ingredient Inventory Card

private struct IngredientInventoryCard: View {
    let ingredients: [IngredientItem]

    private let categories: [String] = ["All"] + IngredientCategory.allCases.map(\.rawValue)

    @State private var selectedCategory: String = "All"

    private var filtered: [IngredientItem] {
        guard selectedCategory != "All" else { return ingredients }
        return ingredients.filter { $0.category.rawValue == selectedCategory }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {

            // Header row
            HStack {
                Text("My Ingredients")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(Color.greenPrimary)

                Spacer()

                Text("\(ingredients.count) items")
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(Color.greenOnBackground.opacity(0.45))
            }

            Spacer().frame(height: 16)

            // Category filter pills
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(categories, id: \.self) { cat in
                        FilterPill(
                            title: cat,
                            isSelected: cat == selectedCategory,
                            onTap: { withAnimation(.easeInOut(duration: 0.18)) { selectedCategory = cat } }
                        )
                    }
                }
                .padding(.vertical, 2)
            }

            Spacer().frame(height: 16)

            // Ingredient rows
            VStack(spacing: 10) {
                ForEach(filtered) { item in
                    IngredientRow(item: item)
                }
            }

            if filtered.isEmpty {
                HStack {
                    Spacer()
                    Text("No items in this category")
                        .font(.system(size: 14))
                        .foregroundColor(Color.greenOnBackground.opacity(0.4))
                        .padding(.vertical, 20)
                    Spacer()
                }
            }
        }
        .padding(24)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 24))
        .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 4)
    }
}

private struct FilterPill: View {
    let title: String
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            Text(title)
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(isSelected ? .white : Color.greenOnBackground.opacity(0.7))
                .padding(.horizontal, 14)
                .padding(.vertical, 7)
                .background(isSelected ? Color.greenPrimary : Color.greenSecondary.opacity(0.4))
                .clipShape(Capsule())
        }
        .buttonStyle(BouncyButtonStyle())
    }
}

// Ingredient Row

private struct IngredientRow: View {
    let item: IngredientItem

    var body: some View {
        HStack(spacing: 14) {
            // Category icon badge
            ZStack {
                RoundedRectangle(cornerRadius: 10)
                    .fill(Color.greenSecondary.opacity(0.35))
                    .frame(width: 40, height: 40)
                Image(systemName: item.category.icon)
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(Color.greenPrimary)
            }

            // Name + category label
            VStack(alignment: .leading, spacing: 2) {
                Text(item.name)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(Color.greenOnBackground)

                Text(item.category.rawValue)
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(Color.greenOnBackground.opacity(0.45))
            }

            Spacer()

            // Quantity badge
            Text(item.quantity)
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(Color.greenPrimary)
                .padding(.horizontal, 10)
                .padding(.vertical, 5)
                .background(Color.greenSecondary.opacity(0.35))
                .clipShape(Capsule())
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 10)
        .background(Color.greenBackground)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(Color.greenSecondary, lineWidth: 1.2)
        )
    }
}

// Buttons

private struct BouncyActionButton: View {
    let text: String
    let container: Color
    let content: Color
    let action: () -> Void

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
