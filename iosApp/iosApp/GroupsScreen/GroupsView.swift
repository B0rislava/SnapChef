//
//  GroupsView.swift
//  iosApp
//
//  Created by gergana on 3/27/26.
//

import SwiftUI

struct GroupsView: View {
    @StateObject private var viewModel = GroupsViewModel()

    @State private var showLeaveConfirm:  Bool = false
    @State private var showDeleteConfirm: Bool = false

    var body: some View {
        ZStack(alignment: .top) {
            LinearGradient(
                colors: [Color.greenSecondary.opacity(0.55), Color.greenBackground],
                startPoint: .top, endPoint: .bottom
            )
            .ignoresSafeArea()

            Circle()
                .fill(Color.greenPrimary.opacity(0.10))
                .frame(width: 240, height: 240)
                .offset(x: UIScreen.main.bounds.width - 60, y: -40)

            // Main scroll content
            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: 16) {
                    Spacer().frame(height: 24)

                    // Header
                    HStack {
                        Text("Groups")
                            .font(.system(size: 28, weight: .heavy))
                            .foregroundColor(Color.greenPrimary)
                        Spacer()
                        Button {
                            viewModel.openDialog(.choice)
                        } label: {
                            Image(systemName: "plus")
                                .font(.system(size: 18, weight: .semibold))
                                .foregroundColor(Color.greenPrimary)
                                .padding(10)
                                .background(Color.greenPrimary.opacity(0.10))
                                .clipShape(Circle())
                        }
                        .buttonStyle(GroupBouncyButtonStyle())
                        .disabled(viewModel.isLoading)
                    }

                    if !viewModel.visibleGroups.isEmpty {
                        // Your Groups card
                        GroupCard {
                            VStack(alignment: .leading, spacing: 12) {
                                Text("Your groups")
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundColor(Color.greenPrimary)

                                ScrollView(.horizontal, showsIndicators: false) {
                                    HStack(spacing: 10) {
                                        ForEach(viewModel.visibleGroups) { group in
                                            GroupPill(
                                                name:       group.name,
                                                isSelected: group.id == viewModel.selectedSharedGroup?.id,
                                                onTap:      { viewModel.selectGroup(group.id) }
                                            )
                                        }
                                    }
                                    .padding(.vertical, 2)
                                }
                            }
                        }
                    }

                    // Selected group detail card
                    if let group = viewModel.selectedSharedGroup {
                        GroupCard {
                            VStack(alignment: .leading, spacing: 20) {

                                // Group name + badges row
                                VStack(alignment: .leading, spacing: 10) {
                                    Text(group.name)
                                        .font(.system(size: 22, weight: .heavy))
                                        .foregroundColor(Color.greenPrimary)

                                    HStack(spacing: 12) {
                                        GroupInfoBadge(
                                            label:     "Admin: \(group.ownerName ?? "Unknown")",
                                            isPrimary: true
                                        )
                                        GroupInfoBadge(
                                            label:     "\(group.members.count) member\(group.members.count == 1 ? "" : "s")",
                                            isPrimary: false
                                        )
                                    }
                                }

                                Divider()
                                    .background(Color.greenSecondary.opacity(0.25))

                                // Members section
                                VStack(alignment: .leading, spacing: 12) {
                                    HStack(spacing: 8) {
                                        Image(systemName: "person.3.fill")
                                            .font(.system(size: 16))
                                            .foregroundColor(Color.greenPrimary)
                                        Text("Members")
                                            .font(.system(size: 16, weight: .bold))
                                            .foregroundColor(Color.greenPrimary)
                                    }

                                    if viewModel.isDetailLoading {
                                        ProgressView()
                                            .progressViewStyle(CircularProgressViewStyle(tint: Color.greenPrimary))
                                            .frame(maxWidth: .infinity, alignment: .center)
                                            .padding(.vertical, 8)
                                    } else if group.members.isEmpty {
                                        Text("No members yet.")
                                            .font(.system(size: 14))
                                            .foregroundColor(Color.greenOnBackground.opacity(0.55))
                                    } else {
                                        VStack(spacing: 10) {
                                            ForEach(group.members) { member in
                                                GroupMemberRow(
                                                    member:  member,
                                                    isOwner: member.name.lowercased() == (group.ownerName ?? "").lowercased(),
                                                    canKick: group.isAdmin && member.name.lowercased() != "you",
                                                    onKick: { viewModel.kickMember(id: member.realId) }
                                                )
                                            }
                                        }
                                    }
                                }

                                Divider()
                                    .background(Color.greenSecondary.opacity(0.25))

                                // Action buttons
                                HStack(spacing: 12) {
                                    GroupActionButton(
                                        text:      "Leave",
                                        icon:      "arrow.right.to.line",
                                        container: Color.greenPrimary.opacity(0.12),
                                        content:   Color.greenPrimary,
                                        action:    { showLeaveConfirm = true }
                                    )
                                    .disabled(viewModel.isLoading)
                                    if group.isAdmin {
                                        GroupActionButton(
                                            text:      "Delete",
                                            icon:      "trash",
                                            container: Color.red.opacity(0.10),
                                            content:   Color.red,
                                            action:    { showDeleteConfirm = true }
                                        )
                                        .disabled(viewModel.isLoading)
                                    }
                                }
                            }
                        }

                        // Join Code card
                        if group.isAdmin {
                            GroupCard {
                                VStack(spacing: 10) {
                                    Text("Group Join Code")
                                        .font(.system(size: 16, weight: .bold))
                                        .foregroundColor(Color.greenPrimary)

                                    Text(group.code ?? "Loading...")
                                        .font(.system(size: 26, weight: .heavy, design: .monospaced))
                                        .foregroundColor(group.code != nil ? Color.greenPrimary : Color.greenOnBackground.opacity(0.4))
                                        .tracking(group.code != nil ? 4 : 0)
                                        .padding(.horizontal, 28)
                                        .padding(.vertical, 14)
                                        .background(Color.greenPrimary.opacity(0.08))
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 16)
                                                .stroke(Color.greenPrimary.opacity(0.30), lineWidth: 1.5)
                                        )
                                        .clipShape(RoundedRectangle(cornerRadius: 16))

                                    Text(group.code != nil
                                         ? "Share this code with others to join your cooking journey!"
                                         : "Fetching invite code...")
                                        .font(.system(size: 13))
                                        .foregroundColor(Color.greenOnBackground.opacity(0.60))
                                        .multilineTextAlignment(.center)
                                }
                                .frame(maxWidth: .infinity, alignment: .center)
                            }
                        }

                    } else if !viewModel.isLoading {
                        GroupCard {
                            VStack(alignment: .leading, spacing: 12) {
                                Text("No shared groups yet")
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundColor(Color.greenPrimary)

                                Text("Create a new group or join with a code to start collaborating.")
                                    .font(.system(size: 14))
                                    .foregroundColor(Color.greenOnBackground.opacity(0.75))
                            }
                        }
                    }

                    Spacer().frame(height: 96)
                }
                .padding(.horizontal, 24)
            }
            .refreshable {
                await viewModel.loadGroups()
            }

            if viewModel.isLoading {
                Color.black.opacity(0.12)
                    .ignoresSafeArea()
                    .allowsHitTesting(true)
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: Color.greenPrimary))
                    .scaleEffect(1.4)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }

            if let msg = viewModel.infoMessage {
                VStack {
                    HStack(spacing: 8) {
                        Image(systemName: viewModel.isError ? "exclamationmark.triangle.fill" : "checkmark.circle.fill")
                            .foregroundColor(.white)
                        Text(msg)
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.white)
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 12)
                    .background(viewModel.isError ? Color.red : Color.greenPrimary)
                    .clipShape(RoundedRectangle(cornerRadius: 20))
                    .shadow(color: .black.opacity(0.15), radius: 8, x: 0, y: 4)
                    .padding(.horizontal, 32)
                    .padding(.top, 16)
                    .transition(.move(edge: .top).combined(with: .opacity))

                    Spacer()
                }
                .animation(.spring(response: 0.4, dampingFraction: 0.75), value: viewModel.infoMessage)
                .zIndex(100)
            }
        }
        // Dialogs
        .sheet(item: $viewModel.dialogMode) { mode in
            dialogSheet(for: mode)
        }
        // Confirmations
        .alert("Leave group?", isPresented: $showLeaveConfirm) {
            Button("Leave", role: .destructive) { viewModel.leaveGroup() }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("Are you sure you want to leave \(viewModel.selectedSharedGroup?.name ?? "this group")?")
        }
        .alert("Delete group?", isPresented: $showDeleteConfirm) {
            Button("Delete", role: .destructive) { viewModel.deleteGroup() }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("Are you sure you want to delete \(viewModel.selectedSharedGroup?.name ?? "this group")? This cannot be undone.")
        }
    }

    @ViewBuilder
    private func dialogSheet(for mode: GroupDialogMode) -> some View {
        switch mode {
        case .choice:
            VStack(spacing: 20) {
                Text("Group options")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(Color.greenPrimary)
                Text("Choose what you want to do next.")
                    .font(.system(size: 14))
                    .foregroundColor(Color.greenOnBackground.opacity(0.7))
                HStack(spacing: 12) {
                    Button("Create group") { viewModel.openDialog(.create) }
                        .buttonStyle(GroupSheetButtonStyle(isPrimary: false))
                        .disabled(viewModel.isLoading)
                    Button("Join group") { viewModel.openDialog(.join) }
                        .buttonStyle(GroupSheetButtonStyle(isPrimary: true))
                        .disabled(viewModel.isLoading)
                }
            }
            .padding(32)
            .presentationDetents([.fraction(0.28)])

        case .join:
            VStack(alignment: .leading, spacing: 16) {
                Text("Join group")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(Color.greenPrimary)
                Text("Enter an invite code to join an existing group.")
                    .font(.system(size: 14))
                    .foregroundColor(Color.greenOnBackground.opacity(0.7))
                TextField("e.g. A7K2P1gg", text: $viewModel.joinCodeInput)
                    .font(.system(size: 16, design: .monospaced))
                    .autocorrectionDisabled()
                    .padding(14)
                    .background(Color.greenSecondary.opacity(0.20))
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                    .overlay(
                        RoundedRectangle(cornerRadius: 14)
                            .stroke(Color.greenPrimary.opacity(0.35), lineWidth: 1)
                    )
                HStack {
                    Button("Cancel") { viewModel.closeDialog() }
                        .buttonStyle(GroupSheetButtonStyle(isPrimary: false))
                    Spacer()
                    Button("Join") { viewModel.joinGroup() }
                        .buttonStyle(GroupSheetButtonStyle(isPrimary: true))
                        .disabled(viewModel.isLoading)
                        .opacity(viewModel.isLoading ? 0.6 : 1.0)
                }
            }
            .padding(32)
            .presentationDetents([.fraction(0.38)])

        case .create:
            VStack(alignment: .leading, spacing: 16) {
                Text("Create group")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(Color.greenPrimary)
                Text("Start a new group. You will be the initial admin.")
                    .font(.system(size: 14))
                    .foregroundColor(Color.greenOnBackground.opacity(0.7))
                TextField("e.g. Weekend Chefs", text: $viewModel.createNameInput)
                    .font(.system(size: 16))
                    .padding(14)
                    .background(Color.greenSecondary.opacity(0.20))
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                    .overlay(
                        RoundedRectangle(cornerRadius: 14)
                            .stroke(Color.greenPrimary.opacity(0.35), lineWidth: 1)
                    )
                HStack {
                    Button("Cancel") { viewModel.closeDialog() }
                        .buttonStyle(GroupSheetButtonStyle(isPrimary: false))
                    Spacer()
                    Button("Create") { viewModel.createGroup() }
                        .buttonStyle(GroupSheetButtonStyle(isPrimary: true))
                        .disabled(viewModel.isLoading)
                        .opacity(viewModel.isLoading ? 0.6 : 1.0)
                }
            }
            .padding(32)
            .presentationDetents([.fraction(0.38)])
        }
    }
}

private struct GroupCard<Content: View>: View {
    @ViewBuilder let content: () -> Content
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            content()
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 24))
        .shadow(color: .black.opacity(0.07), radius: 8, x: 0, y: 4)
    }
}

private struct GroupPill: View {
    let name:       String
    let isSelected: Bool
    let onTap:      () -> Void

    var body: some View {
        Button(action: onTap) {
            Text(name)
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(isSelected ? .white : Color.greenPrimary)
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .background(isSelected ? Color.greenPrimary : Color.white)
                .clipShape(RoundedRectangle(cornerRadius: 18))
                .overlay(
                    RoundedRectangle(cornerRadius: 18)
                        .stroke(
                            isSelected ? Color.clear : Color.greenPrimary.opacity(0.28),
                            lineWidth: 1
                        )
                )
        }
        .buttonStyle(GroupBouncyButtonStyle())
    }
}

private struct GroupInfoBadge: View {
    let label:     String
    let isPrimary: Bool

    var body: some View {
        Text(label)
            .font(.system(size: 12, weight: .semibold))
            .foregroundColor(isPrimary ? Color.greenPrimary : Color.greenOnBackground)
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(isPrimary ? Color.greenPrimary.opacity(0.14) : Color.greenSecondary.opacity(0.50))
            .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

private struct GroupMemberRow: View {
    let member:  GroupMember
    let isOwner: Bool
    var canKick: Bool = false
    var onKick: (() -> Void)? = nil

    private var initials: String {
        let parts = member.name.components(separatedBy: " ").filter { !$0.isEmpty }
        guard !parts.isEmpty else { return "?" }
        if parts.count == 1 { return String(parts[0].prefix(2)).uppercased() }
        return (String(parts[0].first!) + String(parts[parts.count - 1].first!)).uppercased()
    }

    private var isAlt: Bool { member.avatarSeed.hashValue % 2 == 0 }

    var body: some View {
        HStack(spacing: 12) {
            // Avatar bubble
            ZStack {
                Circle()
                    .fill(isAlt ? Color.greenSecondary : Color.greenPrimary.opacity(0.20))
                    .frame(width: 42, height: 42)
                Text(initials)
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(isAlt ? Color.greenOnBackground : Color.greenPrimary)
            }

            Text(member.name)
                .font(.system(size: 15, weight: .semibold))
                .foregroundColor(Color.greenOnBackground)

            Spacer()

            if isOwner {
                GroupInfoBadge(label: "Admin", isPrimary: true)
            } else if canKick {
                Button("Kick") {
                    onKick?()
                }
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(.red)
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 10)
        .background(Color.greenSecondary.opacity(0.12))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color.greenSecondary.opacity(0.55), lineWidth: 1)
        )
    }
}

private struct GroupActionButton: View {
    let text:      String
    let icon:      String
    let container: Color
    let content:   Color
    let action:    () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.system(size: 14))
                Text(text)
                    .font(.system(size: 14, weight: .semibold))
            }
            .foregroundColor(content)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 12)
            .background(container)
            .clipShape(RoundedRectangle(cornerRadius: 14))
        }
        .buttonStyle(GroupBouncyButtonStyle())
    }
}

private struct GroupBouncyButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
            .animation(.spring(response: 0.25, dampingFraction: 0.65), value: configuration.isPressed)
    }
}

private struct GroupSheetButtonStyle: ButtonStyle {
    let isPrimary: Bool
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: 15, weight: .semibold))
            .foregroundColor(isPrimary ? .white : Color.greenPrimary)
            .padding(.horizontal, 24)
            .padding(.vertical, 12)
            .background(isPrimary ? Color.greenPrimary : Color.greenPrimary.opacity(0.08))
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
            .animation(.spring(response: 0.25, dampingFraction: 0.65), value: configuration.isPressed)
    }
}
