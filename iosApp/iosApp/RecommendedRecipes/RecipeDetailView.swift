//
//  RecipeDetailView.swift
//  iosApp
//
//  Created by gergana on 4/8/26.
//
import SwiftUI

struct RecipeDetailView: View {
    let recipe:              RecommendedRecipeItem
    @Binding var checkedIngredients: [String: Bool]
    let infoMessage:         String?
    let onBack:              () -> Void
    let onToggle:            (String, Bool) -> Void
    var onSave:              () -> Void = {}
    var onShare:             () -> Void = {}

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(alignment: .leading, spacing: 16) {
                Spacer().frame(height: 24)

                // Back button + title
                HStack(spacing: 10) {
                    Button(action: onBack) {
                        ZStack {
                            Circle()
                                .fill(Color.greenPrimary.opacity(0.10))
                                .frame(width: 40, height: 40)
                            Image(systemName: "chevron.left")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(Color.greenPrimary)
                        }
                    }
                    .buttonStyle(.plain)

                    Text(NSLocalizedString("recipe_details", comment: ""))
                        .font(.system(size: 17, weight: .bold))
                        .foregroundColor(Color.greenPrimary)
                }

                // Detail card
                VStack(alignment: .leading, spacing: 14) {

                    Text(recipe.title)
                        .font(.system(size: 24, weight: .heavy))
                        .foregroundColor(Color.greenPrimary)

                    HStack(spacing: 8) {
                        RecipePill(isQuick: recipe.isQuick)
                        HStack(spacing: 6) {
                            Image(systemName: "checklist")
                                .font(.system(size: 12))
                                .foregroundColor(Color.greenPrimary)
                            Text("\(recipe.ingredients.count) items")
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundColor(Color.greenOnBackground)
                        }
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(Color.greenSecondary.opacity(0.45))
                        .clipShape(Capsule())
                    }

                    VStack(alignment: .leading, spacing: 4) {
                        Text(NSLocalizedString("description", comment: ""))
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(Color.greenPrimary)
                        Text(recipe.description)
                            .font(.system(size: 15))
                            .foregroundColor(Color.greenOnBackground)
                    }

                    Divider().background(Color.greenSecondary.opacity(0.4))

                    // Ingredients
                    VStack(alignment: .leading, spacing: 10) {
                        Text(NSLocalizedString("ingredients", comment: ""))
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(Color.greenPrimary)

                        VStack(spacing: 8) {
                            ForEach(recipe.ingredients, id: \.self) { ingredient in
                                HStack(spacing: 10) {
                                    Image(systemName: checkedIngredients[ingredient] == true
                                          ? "checkmark.square.fill" : "square")
                                        .font(.system(size: 20))
                                        .foregroundColor(Color.greenPrimary)
                                        .onTapGesture {
                                            onToggle(ingredient, !(checkedIngredients[ingredient] ?? false))
                                        }
                                    Text(ingredient)
                                        .font(.system(size: 15))
                                        .foregroundColor(Color.greenOnBackground)
                                    Spacer()
                                }
                            }
                        }
                        .padding(12)
                        .background(Color.greenBackground)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(Color.greenSecondary.opacity(0.6), lineWidth: 1)
                        )
                    }

                    Divider().background(Color.greenSecondary.opacity(0.4))

                    // Instructions
                    VStack(alignment: .leading, spacing: 10) {
                        Text(NSLocalizedString("instructions", comment: ""))
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(Color.greenPrimary)

                        InstructionsList(instructions: recipe.instructions)
                    }

                    // Checked count
                    let checkedCount = recipe.ingredients.filter { checkedIngredients[$0] == true }.count
                    Text("You have \(checkedCount) of \(recipe.ingredients.count) ingredients ready.")
                        .font(.system(size: 14))
                        .foregroundColor(Color.greenOnBackground.opacity(0.85))
                }
                .padding(24)
                .background(Color.white)
                .clipShape(RoundedRectangle(cornerRadius: 24))
                .shadow(color: .black.opacity(0.07), radius: 8, x: 0, y: 4)

                // Save/Share
                HStack(spacing: 16) {
                    Button(action: onSave) {
                        Text("Save")
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 52)
                            .background(Color.greenPrimary)
                            .clipShape(RoundedRectangle(cornerRadius: 24))
                    }
                    .buttonStyle(RecipeBouncyButtonStyle())

                    Button(action: onShare) {
                        Text("Share")
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(Color.greenPrimary)
                            .frame(maxWidth: .infinity)
                            .frame(height: 52)
                            .background(Color.greenSecondary.opacity(0.5))
                            .clipShape(RoundedRectangle(cornerRadius: 24))
                    }
                    .buttonStyle(RecipeBouncyButtonStyle())
                }

                // Info message
                if let msg = infoMessage {
                    Text(msg)
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(Color.greenPrimary)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 8)
                        .frame(maxWidth: .infinity)
                        .background(Color.greenPrimary.opacity(0.12))
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                        .transition(.opacity)
                        .animation(.easeInOut, value: infoMessage)
                }

                Spacer().frame(height: 96)
            }
            .padding(.horizontal, 24)
        }
    }
}

private struct InstructionsList: View {
    let instructions: [String]

    var body: some View {
        VStack(spacing: 10) {
            ForEach(Array(instructions.enumerated()), id: \.offset) { i, step in
                HStack(alignment: .top, spacing: 10) {
                    ZStack {
                        Circle()
                            .fill(Color.greenPrimary.opacity(0.14))
                            .frame(width: 24, height: 24)
                        Text("\(i + 1)")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(Color.greenPrimary)
                    }
                    Text(step)
                        .font(.system(size: 15))
                        .foregroundColor(Color.greenOnBackground)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
        }
        .padding(12)
        .background(Color.greenBackground)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color.greenSecondary.opacity(0.6), lineWidth: 1)
        )
    }
}
