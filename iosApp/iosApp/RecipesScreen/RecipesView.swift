import SwiftUI
import Shared

struct RecipesView: View {
    @EnvironmentObject private var viewModel: GroupsViewModel

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

            if let recipe = viewModel.selectedRecipe {
                GroupRecipeDetailsView(recipe: recipe, viewModel: viewModel)
            } else {
                ScrollView(showsIndicators: false) {
                    VStack(alignment: .leading, spacing: 20) {
                        Spacer().frame(height: 24)
                        
                        Text("Recipes")
                            .font(.system(size: 28, weight: .heavy))
                            .foregroundColor(Color.greenPrimary)
                            .padding(.horizontal, 24)
                        
                        if let g = viewModel.selectedSharedGroup, !g.isPersonal {
                            VStack(alignment: .leading, spacing: 10) {
                                Text("Group kitchens together")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(Color.greenPrimary)
                                if viewModel.isCombinedLoading {
                                    ProgressView()
                                        .tint(Color.greenPrimary)
                                        .padding(.vertical, 4)
                                } else if let line = viewModel.combinedPantryLabel[g.id] {
                                    Text(line)
                                        .font(.system(size: 13))
                                        .foregroundColor(Color.greenOnBackground.opacity(0.8))
                                        .fixedSize(horizontal: false, vertical: true)
                                } else {
                                    Text("We combine every member’s pantry to plan meals. Pull to load or open another group to refresh.")
                                        .font(.system(size: 13))
                                        .foregroundColor(Color.greenOnBackground.opacity(0.65))
                                }
                                Button {
                                    viewModel.addCombinedGroupMealIdeas()
                                } label: {
                                    Text("Get AI meal ideas from your group’s food")
                                        .font(.system(size: 15, weight: .bold))
                                        .foregroundColor(.white)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 14)
                                        .background(Color.greenPrimary)
                                        .clipShape(RoundedRectangle(cornerRadius: 16))
                                }
                            }
                            .padding(16)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(Color.white)
                            .clipShape(RoundedRectangle(cornerRadius: 20))
                            .shadow(color: .black.opacity(0.05), radius: 6, x: 0, y: 3)
                            .padding(.horizontal, 24)
                        }
                        
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 12) {
                                ForEach(viewModel.groups) { group in
                                    let isSelected = group.id == viewModel.selectedGroup?.id
                                    Button {
                                        viewModel.selectGroup(group.id)
                                    } label: {
                                        Text(group.name)
                                            .font(.system(size: 14, weight: .bold))
                                            .foregroundColor(isSelected ? .white : Color.greenPrimary)
                                            .padding(.horizontal, 16)
                                            .padding(.vertical, 10)
                                            .background(isSelected ? Color.greenPrimary : Color.white)
                                            .clipShape(Capsule())
                                            .overlay(
                                                Capsule()
                                                    .stroke(isSelected ? Color.clear : Color.greenPrimary.opacity(0.3), lineWidth: 1)
                                            )
                                    }
                                }
                            }
                            .padding(.horizontal, 24)
                        }
                        
                        if let msg = viewModel.infoMessage {
                            Text(msg)
                                .font(.system(size: 13))
                                .foregroundColor(Color.greenPrimary)
                                .padding(.horizontal, 24)
                        }
                        
                        let group = viewModel.selectedGroup ?? viewModel.groups.first
                        if let validGroup = group {
                            Text(validGroup.isPersonal ? "Your Saved Recipes" : "\(validGroup.name) Recipes")
                                .font(.system(size: 20, weight: .heavy))
                                .foregroundColor(Color.greenPrimary)
                                .padding(.horizontal, 24)
                                .padding(.top, 8)
                            
                            if validGroup.recipes.isEmpty {
                                Text(validGroup.isPersonal
                                    ? "No recipes saved yet."
                                    : "Nothing shared in this group yet. Open a recipe and tap “Share with a group”, or use the AI meal button above."
                                )
                                    .font(.system(size: 15))
                                    .foregroundColor(Color.greenOnBackground.opacity(0.6))
                                    .frame(maxWidth: .infinity, alignment: .center)
                                    .padding(.top, 32)
                            } else {
                                VStack(spacing: 16) {
                                    ForEach(validGroup.recipes) { recipe in
                                        RecipeCardRow(
                                            recipe: recipe,
                                            viewModel: viewModel
                                        ) {
                                            viewModel.openRecipe(recipe)
                                        }
                                    }
                                }
                                .padding(.horizontal, 24)
                            }
                        }
                        
                        Spacer().frame(height: 96)
                    }
                }
                .refreshable {
                    viewModel.loadCombinedGroupPantrySummary()
                }
            }
        }
        .onAppear {
            if viewModel.selectedSharedGroup != nil {
                viewModel.loadCombinedGroupPantrySummary()
            }
        }
        .overlay(alignment: .top) {
            if let msg = viewModel.infoMessage, viewModel.selectedRecipe != nil {
                Text(msg)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .background(viewModel.isError ? Color.red.opacity(0.9) : Color.greenPrimary)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding(.top, 8)
                    .padding(.horizontal, 16)
            }
        }
    }
}

struct RecipeCardRow: View {
    let recipe: SharedRecipe
    @ObservedObject var viewModel: GroupsViewModel
    let onTap: () -> Void

    private var canBackendFavorite: Bool {
        recipe.catalogRecipeId != nil || recipe.sessionRecipeId != nil
    }

    private var heartOn: Bool {
        if recipe.catalogRecipeId != nil { return recipe.isCatalogStarred == true }
        if recipe.sessionRecipeId != nil { return recipe.isSessionFavorited == true }
        return false
    }

    private var canDeleteFromGroup: Bool { recipe.serverSharedRecipeId != nil }
    
    var body: some View {
        HStack(spacing: 0) {
            let days = recipe.earliestDaysLeft()
            let statusColor: Color = {
                guard let d = days else { return Color.greenPrimary }
                if d < 0 { return Color.gray }
                if d == 0 { return Color(red: 0.77, green: 0.16, blue: 0.16) }
                return Color.greenPrimary
            }()
            let statusText: String = {
                guard let d = days else { return "Saved" }
                if d < 0 { return "Expired ingredients" }
                if d == 0 { return "Requires action today!" }
                return "Expires in \(d) days"
            }()
            
            Rectangle()
                .fill(statusColor)
                .frame(width: 6)
            
            HStack(alignment: .top, spacing: 8) {
                VStack(alignment: .leading, spacing: 6) {
                    Text(recipe.title)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(Color.greenOnBackground)
                        .multilineTextAlignment(.leading)
                        .contentShape(Rectangle())
                        .onTapGesture(perform: onTap)
                    
                    HStack(spacing: 6) {
                        Image(systemName: "person.fill")
                            .font(.system(size: 12))
                            .foregroundColor(Color.greenPrimary)
                        Text(recipe.ownerName == "AI Suggestion" ? "AI suggested for your group" : 
                                (recipe.ownerName == "You" ? "Saved by you" : "Shared by \(recipe.ownerName)"))
                            .font(.system(size: 12, weight: recipe.ownerName == "AI Suggestion" ? .bold : .regular))
                            .foregroundColor(recipe.ownerName == "AI Suggestion" ? Color.greenPrimary : Color.greenOnBackground.opacity(0.7))
                    }
                    .contentShape(Rectangle())
                    .onTapGesture(perform: onTap)
                    
                    Spacer().frame(height: 8)
                    
                    Text(statusText)
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundColor(statusColor)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(statusColor.opacity(0.1))
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                        .contentShape(Rectangle())
                        .onTapGesture(perform: onTap)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                HStack(spacing: 4) {
                    if canDeleteFromGroup {
                        Button {
                            viewModel.deleteSharedRecipeFromGroup(recipe)
                        } label: {
                            Image(systemName: "trash")
                                .font(.system(size: 18))
                                .foregroundColor(Color.greenOnBackground.opacity(0.45))
                        }
                        .buttonStyle(.borderless)
                    }
                    if canBackendFavorite {
                        Button {
                            viewModel.toggleRecipeFavorite(recipe)
                        } label: {
                            Image(systemName: heartOn ? "heart.fill" : "heart")
                                .font(.system(size: 20))
                                .foregroundColor(Color.greenPrimary)
                        }
                        .buttonStyle(.borderless)
                    }
                }
                .padding(.top, 2)
            }
            .padding(16)
        }
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .shadow(color: .black.opacity(0.05), radius: 6, x: 0, y: 3)
    }
}

struct GroupRecipeDetailsView: View {
    let recipe: SharedRecipe
    @ObservedObject var viewModel: GroupsViewModel
    @State private var showGroupSelection = false

    private var canBackendFavorite: Bool {
        recipe.catalogRecipeId != nil || recipe.sessionRecipeId != nil
    }
    private var heartOn: Bool {
        if recipe.catalogRecipeId != nil { return recipe.isCatalogStarred == true }
        if recipe.sessionRecipeId != nil { return recipe.isSessionFavorited == true }
        return false
    }

    private var canRemoveFromGroup: Bool {
        recipe.serverSharedRecipeId != nil && !(viewModel.selectedGroup?.isPersonal ?? true)
    }

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(alignment: .leading, spacing: 16) {
                Spacer().frame(height: 24)
                
                HStack(spacing: 12) {
                    Button(action: { viewModel.closeRecipeDetails() }) {
                        Image(systemName: "arrow.backward")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(Color.greenPrimary)
                            .frame(width: 40, height: 40)
                            .background(Color.greenPrimary.opacity(0.1))
                            .clipShape(Circle())
                    }
                    Text("Recipe Details")
                        .font(.system(size: 24, weight: .heavy))
                        .foregroundColor(Color.greenPrimary)
                }
                .padding(.horizontal, 24)
                
                RecipesCardView {
                    VStack(alignment: .leading, spacing: 12) {
                        HStack(alignment: .top) {
                            Text(recipe.title)
                                .font(.system(size: 20, weight: .heavy))
                                .foregroundColor(Color.greenPrimary)
                                .lineLimit(nil)
                            Spacer(minLength: 8)
                            if canBackendFavorite {
                                Button {
                                    viewModel.toggleRecipeFavorite(recipe)
                                } label: {
                                    Image(systemName: heartOn ? "heart.fill" : "heart")
                                        .font(.system(size: 22))
                                        .foregroundColor(Color.greenPrimary)
                                }
                                .buttonStyle(.borderless)
                            }
                            Text(recipe.ownerName == "AI Suggestion" ? "AI Group Suggestion" : 
                                    (recipe.ownerName == "You" ? "Saved" : "Shared"))
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(Color.greenPrimary)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 6)
                                .background(recipe.ownerName == "AI Suggestion" ? Color.greenPrimary.opacity(0.15) : Color.greenSecondary.opacity(0.3))
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                        
                        if !recipe.description.isEmpty {
                            Text(recipe.description)
                                .font(.system(size: 15))
                                .foregroundColor(Color.greenOnBackground.opacity(0.8))
                        }
                        
                        Spacer().frame(height: 12)
                        
                        if canRemoveFromGroup {
                            Button(action: { viewModel.deleteSharedRecipeFromGroup(recipe) }) {
                                Text("Remove from group")
                                    .font(.system(size: 15, weight: .bold))
                                    .foregroundColor(Color(red: 0.78, green: 0.16, blue: 0.16))
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 14)
                                    .background(Color(red: 0.78, green: 0.16, blue: 0.16).opacity(0.12))
                                    .clipShape(RoundedRectangle(cornerRadius: 16))
                            }
                            Spacer().frame(height: 12)
                        }
                        Button(action: { showGroupSelection = true }) {
                            Text("Share with a group")
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 14)
                                .background(Color.greenPrimary)
                                .clipShape(RoundedRectangle(cornerRadius: 16))
                        }
                    }
                    .padding(24)
                }
                .padding(.horizontal, 24)
                
                RecipesCardView {
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Ingredients")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(Color.greenPrimary)
                        
                        let spoiled = recipe.spoiledProducts()
                        
                        let columns = [GridItem(.adaptive(minimum: 120), spacing: 8, alignment: .leading)]
                        LazyVGrid(columns: columns, alignment: .leading, spacing: 8) {
                            ForEach(recipe.availableItems, id: \.self) { item in
                                AvailableItemView(item: item)
                            }
                            
                            ForEach(recipe.missingItems, id: \.self) { item in
                                let isSpoiled = spoiled.contains { item.localizedCaseInsensitiveContains($0) }
                                MissingItemView(item: item, isSpoiled: isSpoiled)
                            }
                        }
                    }
                    .padding(24)
                }
                .padding(.horizontal, 24)
                
                if !recipe.instructions.isEmpty {
                    RecipesCardView {
                        VStack(alignment: .leading, spacing: 20) {
                            Text("Instructions")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(Color.greenPrimary)
                            
                            ForEach(Array(recipe.instructions.enumerated()), id: \.offset) { index, step in
                                HStack(alignment: .top, spacing: 16) {
                                    Text("\(index + 1)")
                                        .font(.system(size: 11, weight: .heavy))
                                        .foregroundColor(Color.greenPrimary)
                                        .frame(width: 24, height: 24)
                                        .background(Color.greenPrimary.opacity(0.15))
                                        .clipShape(Circle())
                                    
                                    Text(step)
                                        .font(.system(size: 15))
                                        .foregroundColor(Color.greenOnBackground.opacity(0.85))
                                        .fixedSize(horizontal: false, vertical: true)
                                }
                            }
                        }
                        .padding(24)
                    }
                    .padding(.horizontal, 24)
                }
                
                Spacer().frame(height: 96)
            }
        }
        .sheet(isPresented: $showGroupSelection) {
            GroupSelectionSheet(viewModel: viewModel) { group in
                viewModel.shareRecipeToGroup(recipe, to: group)
                showGroupSelection = false
            }
        }
    }
}

struct AvailableItemView: View {
    let item: String
    
    var body: some View {
        let parts = item.components(separatedBy: " (from ")
        let name = parts[0]
        let contributor = parts.count > 1 ? parts[1].replacingOccurrences(of: ")", with: "") : nil
        
        HStack(spacing: 6) {
            Text(name)
                .font(.system(size: 13, weight: .bold))
                .foregroundColor(Color.greenPrimary)
            
            if let c = contributor {
                Text(c)
                    .font(.system(size: 10, weight: .black))
                    .foregroundColor(.white)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(Color.greenPrimary)
                    .clipShape(Capsule())
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 6)
        .background(Color.greenPrimary.opacity(0.1))
        .overlay(Capsule().stroke(Color.greenPrimary.opacity(0.8), lineWidth: 1))
        .clipShape(Capsule())
    }
}

struct MissingItemView: View {
    let item: String
    let isSpoiled: Bool
    
    var body: some View {
        Text(item)
            .font(.system(size: 13, weight: .medium))
            .foregroundColor(isSpoiled ? Color(red: 0.77, green: 0.16, blue: 0.16) : Color.gray)
            .strikethrough(isSpoiled)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(isSpoiled ? Color.red.opacity(0.1) : Color.gray.opacity(0.1))
            .overlay(Capsule().stroke(isSpoiled ? Color.red.opacity(0.5) : Color.gray.opacity(0.3), lineWidth: 1))
            .clipShape(Capsule())
    }
}

struct RecipesCardView<Content: View>: View {
    @ViewBuilder let content: () -> Content
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            content()
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 24))
        .shadow(color: .black.opacity(0.07), radius: 8, x: 0, y: 4)
    }
}

struct GroupSelectionSheet: View {
    @ObservedObject var viewModel: GroupsViewModel
    let onSelect: (AppGroup) -> Void
    @Environment(\.presentationMode) var presentationMode

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Select Group")
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(Color.greenPrimary)
                .padding(.top, 16)
            
            Text("Pick a group. Everyone in it will see this recipe in the group list.")
                .font(.system(size: 15))
                .foregroundColor(Color.greenOnBackground.opacity(0.7))
            
            let groups = viewModel.groups.filter { !$0.isPersonal }
            
            if groups.isEmpty {
                Text("You are not in any groups yet. Create or join a group first!")
                    .font(.system(size: 14))
                    .foregroundColor(Color.gray)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(16)
            } else {
                ScrollView {
                    VStack(spacing: 12) {
                        ForEach(groups) { group in
                            Button(action: { onSelect(group) }) {
                                HStack(spacing: 12) {
                                    Image(systemName: "person.3.fill")
                                        .foregroundColor(Color.greenPrimary)
                                    Text(group.name)
                                        .font(.system(size: 16, weight: .bold))
                                        .foregroundColor(Color.greenOnBackground)
                                    Spacer()
                                }
                                .padding(16)
                                .background(Color.greenPrimary.opacity(0.05))
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.greenPrimary.opacity(0.1), lineWidth: 1))
                            }
                        }
                    }
                }
            }
            
            Button("Cancel") {
                presentationMode.wrappedValue.dismiss()
            }
            .font(.system(size: 16, weight: .semibold))
            .foregroundColor(Color.greenPrimary)
            .frame(maxWidth: .infinity, alignment: .trailing)
            .padding(.top, 8)
        }
        .padding(24)
    }
}
