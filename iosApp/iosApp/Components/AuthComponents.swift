
import SwiftUI

struct AuthTextField: View {
    @Binding var value: String
    var placeholder: String
    var leadingIcon: AnyView? = nil
    var trailingIcon: AnyView? = nil
    var isSecure: Bool = false
    var keyboardType: UIKeyboardType = .default

    @FocusState private var isFocused: Bool

    var body: some View {
        HStack(spacing: 8) {
            if let leading = leadingIcon {
                leading.frame(width: 24, height: 24).padding(.leading, 12)
            }

            Group {
                if isSecure {
                    SecureField("", text: $value)
                        .focused($isFocused)
                } else {
                    TextField("", text: $value)
                        .focused($isFocused)
                        .keyboardType(keyboardType)
                        .autocapitalization(.none)
                        .disableAutocorrection(true)
                }
            }
            .placeholder(when: value.isEmpty) {
                Text(placeholder)
                    .font(.system(size: 14))
                    .foregroundColor(Color.greenOnBackground.opacity(0.38))
            }
            .font(.system(size: 14))
            .foregroundColor(Color.greenOnBackground)
            .padding(.vertical, 16)

            if let trailing = trailingIcon {
                trailing.frame(width: 24, height: 24).padding(.trailing, 12)
            }
        }
        .background(Color.white)
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(
                    isFocused ? Color.greenPrimary : Color.greenSecondary,
                    lineWidth: isFocused ? 2 : 1.5
                )
                .animation(.easeInOut(duration: 0.2), value: isFocused)
        )
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .onTapGesture { isFocused = true }
    }
}

struct OrDivider: View {
    var body: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(Color.greenSecondary)
                .frame(height: 1)

            Text("  Or  ")
                .font(.system(size: 14))
                .foregroundColor(Color.greenOnBackground.opacity(0.45))
                .fixedSize()

            Rectangle()
                .fill(Color.greenSecondary)
                .frame(height: 1)
        }
        .frame(maxWidth: .infinity)
    }
}

struct SocialButton: View {
    var label: String
    var emoji: String
    var action: () -> Void = {}

    var body: some View {
        Button(action: action) {
            HStack(spacing: 10) {
                Text(emoji)
                    .font(.system(size: 18))
                Text(label)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(Color.greenOnBackground)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 52)
            .background(Color.white)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(Color.greenSecondary, lineWidth: 1.5)
            )
            .clipShape(RoundedRectangle(cornerRadius: 16))
        }
    }
}

extension View {
    func placeholder<Content: View>(
        when shouldShow: Bool,
        @ViewBuilder placeholder: () -> Content
    ) -> some View {
        ZStack(alignment: .leading) {
            if shouldShow { placeholder() }
            self
        }
    }
}
