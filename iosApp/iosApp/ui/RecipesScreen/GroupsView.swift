//
//  GroupsView.swift
//  iosApp
//
//  Created by gergana on 3/27/26.
//

import SwiftUI

struct GroupsView: View {
    @StateObject private var viewModel = GroupsViewModel()

    var body: some View {
        ZStack(alignment: .bottom) {
            LinearGradient(
                colors: [Color.greenSecondary.opacity(0.55), Color.greenBackground],
                startPoint: .top, endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(alignment: .leading, spacing: 0) {
                HStack {
                    Text("Groups")
                        .font(.title.bold())
                        .foregroundColor(.greenPrimary)
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
                .padding(20)

                RecipesView()
            }
        }
        .sheet(item: $viewModel.dialogMode) { mode in
            dialogSheet(for: mode)
        }
    }

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
