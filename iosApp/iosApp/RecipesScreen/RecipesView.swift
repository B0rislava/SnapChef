//
//  RecipesView.swift
//  iosApp
//
//  Created by gergana on 3/27/26.
//

import SwiftUI

struct RecipesView: View {
    @StateObject private var viewModel = GroupsViewModel()

    var body: some View {
        ZStack {
            if let recipe = viewModel.selectedRecipe {
                GroupRecipeDetails(
                    recipe: recipe,
                    onBack: viewModel.closeRecipeDetails
                )
            } else if let group = viewModel.selectedGroup {
                mainView(group: group)
            }
        }
        .sheet(item: $viewModel.dialogMode) { mode in
            dialogSheet(for: mode)
        }
    }

    private func mainView(group: RecipeGroup) -> some View {
        ZStack(alignment: .topTrailing) {
            LinearGradient(
                colors: [Color.greenSecondary.opacity(0.55), Color.greenBackground],
                startPoint: .top, endPoint: .bottom
            )
            .ignoresSafeArea()

            RoundedRectangle(cornerRadius: 80)
                .fill(Color.greenPrimary.opacity(0.10))
                .frame(width: 240, height: 240)
                .offset(x: 60, y: -40)

            ScrollView {
                VStack(alignment: .leading, spacing: 16) {

                    // Header
                    Text("Recipes")
                        .font(.title.bold())
                        .foregroundColor(.greenPrimary)

                    // Group chip selector
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 12) {
                            ForEach(viewModel.groups) { g in
                                let isSelected = group.id == g.id
                                Text(g.name)
                                    .font(.subheadline.bold())
                                    .foregroundColor(isSelected ? .white : .greenPrimary)
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 10)
                                    .background(isSelected ? Color.greenPrimary : Color.white)
                                    .clipShape(RoundedRectangle(cornerRadius: 20))
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 20)
                                            .stroke(Color.greenPrimary.opacity(isSelected ? 1 : 0.3), lineWidth: 1)
                                    )
                                    .onTapGesture { viewModel.selectGroup(g.id) }
                            }
                        }
                    }

                    // Group code badge
                    if !group.isPersonal, let code = group.code {
                        HStack(spacing: 6) {
                            Text("Group Code:")
                                .font(.caption)
                                .foregroundColor(.greenOnBackground.opacity(0.7))
                            Text(code)
                                .font(.caption.bold())
                                .foregroundColor(.greenPrimary)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color.greenPrimary.opacity(0.15))
                                .clipShape(RoundedRectangle(cornerRadius: 8))
                        }
                    }

                    // Info message
                    if let msg = viewModel.infoMessage {
                        Text(msg)
                            .font(.caption)
                            .foregroundColor(.greenPrimary)
                    }

                    // Section title
                    Text(group.isPersonal ? "Your Saved Recipes" : "\(group.name) Recipes")
                        .font(.title3.bold())
                        .foregroundColor(.greenPrimary)

                    // Recipe list
                    if group.recipes.isEmpty {
                        HStack {
                            Spacer()
                            Text("No recipes saved yet.")
                                .foregroundColor(.greenOnBackground.opacity(0.6))
                            Spacer()
                        }
                        .padding(.top, 32)
                    } else {
                        ForEach(group.recipes) { recipe in
                            RecipeRowCard(recipe: recipe)
                                .onTapGesture { viewModel.openRecipe(recipe) }
                        }
                    }
                }
                .padding(20)
            }

            // Add group button
            VStack {
                HStack {
                    Spacer()
                    Button {
                        viewModel.openDialog(.choice)
                    } label: {
                        Image(systemName: "plus")
                            .foregroundColor(.greenPrimary)
                            .padding(8)
                            .background(Color.greenPrimary.opacity(0.08))
                            .clipShape(Circle())
                    }
                }
                .padding(.top, 20)
                .padding(.trailing, 20)
                Spacer()
            }
        }
    }

    // Dialogs

    @ViewBuilder
    private func dialogSheet(for mode: GroupDialogMode) -> some View {
        switch mode {
        case .choice:
            VStack(spacing: 20) {
                Text("Group options").font(.title3.bold())
                Text("Choose what you want to do.").foregroundColor(.secondary)
                HStack(spacing: 12) {
                    Button("Create group") { viewModel.openDialog(.create) }
                        .buttonStyle(.bordered)
                    Button("Join group") { viewModel.openDialog(.join) }
                        .buttonStyle(.borderedProminent)
                        .tint(.greenPrimary)
                }
            }
            .padding(30)
            .presentationDetents([.fraction(0.30)])

        case .join:
            VStack(spacing: 20) {
                Text("Join group").font(.title3.bold())
                TextField("Enter group code", text: $viewModel.joinCodeInput)
                    .textFieldStyle(.roundedBorder)
                    .autocorrectionDisabled()
                    .textInputAutocapitalization(.characters)
                HStack {
                    Button("Cancel") { viewModel.closeDialog() }.buttonStyle(.bordered)
                    Spacer()
                    Button("Join") { viewModel.joinGroup() }
                        .buttonStyle(.borderedProminent)
                        .tint(.greenPrimary)
                }
            }
            .padding(30)
            .presentationDetents([.fraction(0.35)])

        case .create:
            VStack(spacing: 20) {
                Text("Create group").font(.title3.bold())
                TextField("Group name", text: $viewModel.createNameInput)
                    .textFieldStyle(.roundedBorder)
                HStack {
                    Button("Cancel") { viewModel.closeDialog() }.buttonStyle(.bordered)
                    Spacer()
                    Button("Create") { viewModel.createGroup() }
                        .buttonStyle(.borderedProminent)
                        .tint(.greenPrimary)
                }
            }
            .padding(30)
            .presentationDetents([.fraction(0.35)])
        }
    }
}

// Recipe Row Card

private struct RecipeRowCard: View {
    let recipe: SharedRecipe

    private var days: Int? { recipe.earliestDaysLeft() }

    private var statusColor: Color {
        guard let d = days else { return .greenPrimary }
        if d < 0 { return .gray }
        if d == 0 { return Color(red: 0.78, green: 0.16, blue: 0.16) }
        return .greenPrimary
    }

    private var statusText: String {
        guard let d = days else { return "Saved" }
        if d < 0 { return "Expired ingredients" }
        if d == 0 { return "Requires action today!" }
        return "Expires in \(d) days"
    }

    var body: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(statusColor)
                .frame(width: 6)

            VStack(alignment: .leading, spacing: 6) {
                Text(recipe.title)
                    .font(.headline)
                    .foregroundColor(.greenOnBackground)

                HStack(spacing: 6) {
                    Image(systemName: "person.fill")
                        .font(.system(size: 12))
                        .foregroundColor(.greenPrimary)
                    Text(recipe.ownerName == "You" ? "Saved by you" : "Shared by \(recipe.ownerName)")
                        .font(.caption)
                        .foregroundColor(.greenOnBackground.opacity(0.7))
                }

                Text(statusText)
                    .font(.caption.bold())
                    .foregroundColor(statusColor)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(statusColor.opacity(0.1))
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            }
            .padding(16)

            Spacer()
        }
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .shadow(color: .black.opacity(0.06), radius: 4, x: 0, y: 2)
    }
}

// Recipe Details

private struct GroupRecipeDetails: View {
    let recipe: SharedRecipe
    let onBack: () -> Void

    @State private var inviteMessage: String? = nil

    private var spoiled: [String] { recipe.spoiledProducts() }

    var body: some View {
        ZStack(alignment: .topTrailing) {
            LinearGradient(
                colors: [Color.greenSecondary.opacity(0.45), Color.greenBackground],
                startPoint: .top, endPoint: .bottom
            )
            .ignoresSafeArea()

            RoundedRectangle(cornerRadius: 70)
                .fill(Color.greenPrimary.opacity(0.10))
                .frame(width: 220, height: 220)
                .offset(x: 50, y: -20)

            ScrollView {
                VStack(alignment: .leading, spacing: 16) {

                    // Nav row
                    HStack(spacing: 10) {
                        Button(action: onBack) {
                            Image(systemName: "arrow.left")
                                .foregroundColor(.greenPrimary)
                                .padding(8)
                                .background(Color.greenPrimary.opacity(0.10))
                                .clipShape(Circle())
                        }
                        Text("Recipe Details")
                            .font(.title2.bold())
                            .foregroundColor(.greenPrimary)
                        Spacer()
                    }
                    .padding(.bottom, 8)

                    // Header card
                    VStack(alignment: .leading, spacing: 12) {
                        HStack(alignment: .top) {
                            Text(recipe.title)
                                .font(.title2.bold())
                                .foregroundColor(.greenPrimary)
                            Spacer()
                            Text(recipe.ownerName == "You" ? "Saved" : "Shared")
                                .font(.caption.bold())
                                .foregroundColor(.greenPrimary)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(Color.greenSecondary.opacity(0.3))
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                        }

                        if !recipe.description.isEmpty {
                            Text(recipe.description)
                                .font(.body)
                                .foregroundColor(.greenOnBackground.opacity(0.8))
                                .lineSpacing(4)
                        }

                        if recipe.ownerName != "You" {
                            Button {
                                inviteMessage = "Invitation sent to \(recipe.ownerName)."
                            } label: {
                                Text("Invite to cook together")
                                    .font(.subheadline.bold())
                                    .foregroundColor(.white)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 14)
                                    .background(Color.greenPrimary)
                                    .clipShape(RoundedRectangle(cornerRadius: 16))
                            }

                            if let msg = inviteMessage {
                                Text(msg)
                                    .font(.caption)
                                    .foregroundColor(.greenPrimary)
                                    .frame(maxWidth: .infinity)
                                    .multilineTextAlignment(.center)
                            }
                        }
                    }
                    .padding(24)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 24))
                    .shadow(color: .black.opacity(0.05), radius: 4, x: 0, y: 2)

                    // Ingredients card
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Ingredients")
                            .font(.headline)
                            .foregroundColor(.greenPrimary)

                        FlowLayout(spacing: 8) {
                            ForEach(recipe.availableItems, id: \.self) { item in
                                Text(item)
                                    .font(.caption.bold())
                                    .foregroundColor(.greenPrimary)
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(Color.greenPrimary.opacity(0.1))
                                    .clipShape(RoundedRectangle(cornerRadius: 16))
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 16)
                                            .stroke(Color.greenPrimary.opacity(0.8), lineWidth: 1)
                                    )
                            }

                            ForEach(recipe.missingItems, id: \.self) { item in
                                let isSpoiled = spoiled.contains { item.localizedCaseInsensitiveContains($0) }
                                Text(item)
                                    .font(.caption)
                                    .fontWeight(isSpoiled ? .medium : .regular)
                                    .foregroundColor(isSpoiled ? Color(red: 0.78, green: 0.16, blue: 0.16) : Color.gray)
                                    .strikethrough(isSpoiled)
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(isSpoiled ? Color(red: 1, green: 0.92, blue: 0.92) : Color(white: 0.96))
                                    .clipShape(RoundedRectangle(cornerRadius: 16))
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 16)
                                            .stroke(isSpoiled ? Color(red: 0.9, green: 0.44, blue: 0.44) : Color(white: 0.88), lineWidth: 1)
                                    )
                            }
                        }
                    }
                    .padding(24)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 24))
                    .shadow(color: .black.opacity(0.05), radius: 4, x: 0, y: 2)

                    // Instructions card
                    if !recipe.instructions.isEmpty {
                        VStack(alignment: .leading, spacing: 20) {
                            Text("Instructions")
                                .font(.headline)
                                .foregroundColor(.greenPrimary)

                            ForEach(Array(recipe.instructions.enumerated()), id: \.offset) { index, step in
                                HStack(alignment: .top, spacing: 16) {
                                    ZStack {
                                        Circle()
                                            .fill(Color.greenPrimary.opacity(0.15))
                                            .frame(width: 26, height: 26)
                                        Text("\(index + 1)")
                                            .font(.caption.bold())
                                            .foregroundColor(.greenPrimary)
                                    }
                                    Text(step)
                                        .font(.body)
                                        .foregroundColor(.greenOnBackground.opacity(0.85))
                                        .lineSpacing(4)
                                }
                            }
                        }
                        .padding(24)
                        .background(Color.white)
                        .clipShape(RoundedRectangle(cornerRadius: 24))
                        .shadow(color: .black.opacity(0.05), radius: 4, x: 0, y: 2)
                    }
                }
                .padding(20)
            }
        }
    }
}

private struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let width = proposal.width ?? 0
        var height: CGFloat = 0
        var x: CGFloat = 0
        var rowHeight: CGFloat = 0

        for view in subviews {
            let size = view.sizeThatFits(.unspecified)
            if x + size.width > width, x > 0 {
                height += rowHeight + spacing
                x = 0
                rowHeight = 0
            }
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
        height += rowHeight
        return CGSize(width: width, height: height)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var x = bounds.minX
        var y = bounds.minY
        var rowHeight: CGFloat = 0

        for view in subviews {
            let size = view.sizeThatFits(.unspecified)
            if x + size.width > bounds.maxX, x > bounds.minX {
                y += rowHeight + spacing
                x = bounds.minX
                rowHeight = 0
            }
            view.place(at: CGPoint(x: x, y: y), proposal: ProposedViewSize(size))
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
    }
}
