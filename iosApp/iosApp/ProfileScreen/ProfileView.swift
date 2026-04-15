//
//  ProfileView.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//

import SwiftUI

private enum IngredientCategory: String, CaseIterable {
    case produce = "Produce"
    case dairy   = "Dairy"
    case protein = "Protein"
    case pantry  = "Pantry"

    var systemIcon: String {
        switch self {
        case .produce: return "leaf"
        case .dairy:   return "drop"
        case .protein: return "flame"
        case .pantry:  return "archivebox"
        }
    }
}


struct ProfileView: View {

    @StateObject private var viewModel = ProfileViewModel()

    @State private var isEditingProfile  = false
    @State private var showLogoutDialog  = false
    @State private var showDeleteDialog  = false
    @State private var infoMessage: String? = nil

    var onBack:          () -> Void = {}
    var onLogout:        () -> Void = {}
    var onDeleteAccount: () -> Void = {}


    private var initials: String { viewModel.userName.toInitials() }

    var body: some View {
        Group {
            if isEditingProfile {
                EditProfileView(
                    userName:        viewModel.userName,
                    userEmail:       viewModel.userEmail,
                    profileImageUri: viewModel.profileImageUri,
                    onSave: { name, email, _, _ in
                        viewModel.updateUser(name: name, email: email)
                        withAnimation { isEditingProfile = false }
                    },
                    onCancel: {
                        withAnimation { isEditingProfile = false }
                    }
                )
            } else {
                profileContent
            }
        }
        .onChange(of: infoMessage) { _, msg in
            guard msg != nil else { return }
            Task {
                try? await Task.sleep(nanoseconds: 2_500_000_000)
                infoMessage = nil
            }
        }
        .alert("Log out", isPresented: $showLogoutDialog) {
            Button("Yes", role: .destructive) {
                viewModel.logout()
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                    onLogout()
                }
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("Are you sure you want to log out?")
        }
        .alert("Delete account", isPresented: $showDeleteDialog) {
            Button("Delete", role: .destructive) {
                viewModel.deleteAccount(onSuccess: {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                        onDeleteAccount()
                    }
                })
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("Are you sure you want to delete your account? This action cannot be undone.")
        }
    }

    private var profileContent: some View {
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

                    HStack {
                        Text("Your Profile")
                            .font(.system(size: 28, weight: .heavy))
                            .foregroundColor(Color.greenPrimary)
                        Spacer()
                    }

                    Spacer().frame(height: 32)

                    Circle()
                        .fill(Color.white)
                        .frame(width: 140, height: 140)
                        .overlay(
                            ProfilePhoto(
                                imageUri: viewModel.profileImageUri,
                                initials: initials
                            )
                            .frame(width: 128, height: 128)
                        )

                    Spacer().frame(height: 32)
                    
                    VStack(spacing: 16) {
                        ReadOnlyProfileField(
                            label: "Name",
                            value: viewModel.userName,
                            icon:  "person"
                        )
                        ReadOnlyProfileField(
                            label: "Email",
                            value: viewModel.userEmail,
                            icon:  "envelope"
                        )
                    }
                    .padding(24)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 24))
                    .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 4)

                    if let msg = infoMessage {
                        Spacer().frame(height: 12)
                        Text(msg)
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(Color.greenPrimary)
                            .padding(.horizontal, 14)
                            .padding(.vertical, 10)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(Color.greenPrimary.opacity(0.12))
                            .clipShape(RoundedRectangle(cornerRadius: 14))
                            .transition(.opacity)
                    }

                    if let errMsg = viewModel.errorMessage {
                        Spacer().frame(height: 12)
                        Text(errMsg)
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(.red)
                            .padding(.horizontal, 14)
                            .padding(.vertical, 10)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(Color.red.opacity(0.08))
                            .clipShape(RoundedRectangle(cornerRadius: 14))
                            .transition(.opacity)
                    }

                    Spacer().frame(height: 24)

                    ZStack {
                        IngredientInventoryCard(items: viewModel.inventoryItems)
                        if viewModel.isLoading {
                            RoundedRectangle(cornerRadius: 24)
                                .fill(Color.white.opacity(0.6))
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: Color.greenPrimary))
                                .scaleEffect(1.2)
                        }
                    }

                    Spacer().frame(height: 32)
                    HStack(spacing: 16) {
                        BouncyActionButton(
                            text:      "Logout",
                            container: Color.greenPrimary,
                            content:   .white,
                            action:    { showLogoutDialog = true }
                        )
                        BouncyActionButton(
                            text:      "Delete",
                            container: Color.greenSecondary.opacity(0.5),
                            content:   Color.greenPrimary,
                            action:    { showDeleteDialog = true }
                        )
                        BouncyActionButton(
                            text:      "Edit",
                            container: Color.greenSecondary,
                            content:   Color.greenOnBackground,
                            action: {
                                infoMessage = "Opening profile editor..."
                                withAnimation { isEditingProfile = true }
                            }
                        )
                    }

                    Spacer().frame(height: 32)
                }
                .padding(.horizontal, 24)
                .safeAreaInset(edge: .bottom) { Color.clear.frame(height: 76) }
            }
            .refreshable {
                viewModel.loadInventory()
            }
        }
        .navigationBarHidden(true)
    }
}


private struct ReadOnlyProfileField: View {
    let label: String
    let value: String
    let icon:  String

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

private struct IngredientInventoryCard: View {
    let items: [ProfileInventoryItem]

    @State private var selectedCategory: String = "All"

    private var categories: [String] {
        ["All"] + items.map(\.category).removingDuplicates()
    }

    private var filtered: [ProfileInventoryItem] {
        guard selectedCategory != "All" else { return items }
        return items.filter { $0.category == selectedCategory }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {

            HStack {
                Text("My Ingredients")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(Color.greenPrimary)
                Spacer()
                Text("\(items.count) items")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(Color.greenOnBackground.opacity(0.65))
            }

            Spacer().frame(height: 14)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(categories, id: \.self) { cat in
                        let selected = cat == selectedCategory
                        Button {
                            withAnimation(.easeInOut(duration: 0.18)) {
                                selectedCategory = cat
                            }
                        } label: {
                            Text(cat)
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundColor(selected ? .white : Color.greenOnBackground.opacity(0.75))
                                .padding(.horizontal, 14)
                                .padding(.vertical, 8)
                                .background(selected ? Color.greenPrimary : Color.greenSecondary.opacity(0.30))
                                .clipShape(RoundedRectangle(cornerRadius: 14))
                        }
                        .buttonStyle(BouncyButtonStyle())
                    }
                }
                .padding(.vertical, 2)
            }

            Spacer().frame(height: 14)

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

private struct IngredientRow: View {
    let item: ProfileInventoryItem

    private var categoryIcon: String {
        switch item.category.lowercased() {
        case "protein": return "flame"
        case "dairy":   return "drop"
        case "produce": return "leaf.fill"
        default:        return "archivebox"
        }
    }

    var body: some View {
        HStack(spacing: 12) {
            ZStack {
                RoundedRectangle(cornerRadius: 12)
                    .fill(Color.greenSecondary.opacity(0.35))
                    .frame(width: 40, height: 40)
                Image(systemName: categoryIcon)
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(Color.greenPrimary)
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(item.name)
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(Color.greenOnBackground)
                Text(item.category)
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(Color.greenOnBackground.opacity(0.65))
            }

            Spacer()

            Text(item.quantity)
                .font(.system(size: 13, weight: .bold))
                .foregroundColor(Color.greenPrimary)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(Color.greenSecondary.opacity(0.45))
                .clipShape(Capsule())
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 10)
        .background(Color.greenSecondary.opacity(0.20))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color.greenSecondary.opacity(0.65), lineWidth: 1)
        )
    }
}

private struct BouncyActionButton: View {
    let text:      String
    let container: Color
    let content:   Color
    let action:    () -> Void

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

private extension Array where Element: Equatable {
    func removingDuplicates() -> [Element] {
        var seen: [Element] = []
        var result: [Element] = []
        for element in self {
            if !seen.contains(element) {
                seen.append(element)
                result.append(element)
            }
        }
        return result
    }
}
