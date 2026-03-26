//
//  SnapChefTheme.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//
import SwiftUI

// pallete
extension Color {
    static let greenPrimary       = Color(hex: 0x587E3D)
    static let greenSecondary     = Color(hex: 0xCED8B7)
    static let greenBackground    = Color(hex: 0xF5FBF0)
    static let greenSurface       = Color(hex: 0xFFFFFF)
    static let greenOnPrimary     = Color(hex: 0xFFFFFF)
    static let greenOnSecondary   = Color(hex: 0x2D3D1A)
    static let greenOnBackground  = Color(hex: 0x1C2B10)
    static let greenOnSurface     = Color(hex: 0x1C2B10)
    static let greenError         = Color(hex: 0xB00020)

    // Hex initializer
    init(hex: UInt32) {
        let r = Double((hex >> 16) & 0xFF) / 255
        let g = Double((hex >> 8)  & 0xFF) / 255
        let b = Double( hex        & 0xFF) / 255
        self.init(red: r, green: g, blue: b)
    }
}

// typography
extension Font {
    static let snapDisplayLarge    = Font.system(size: 36, weight: .bold,      design: .default)
    static let snapHeadlineMedium  = Font.system(size: 26, weight: .bold,      design: .default)
    static let snapTitleLarge      = Font.system(size: 20, weight: .semibold,  design: .default)
    static let snapBodyLarge       = Font.system(size: 16, weight: .regular,   design: .default)
    static let snapBodyMedium      = Font.system(size: 14, weight: .regular,   design: .default)
    static let snapLabelLarge      = Font.system(size: 15, weight: .semibold,  design: .default)
}

struct SnapChefThemeKey: EnvironmentKey {
    static let defaultValue = SnapChefTokens()
}

extension EnvironmentValues {
    var snapTheme: SnapChefTokens {
        get { self[SnapChefThemeKey.self] }
        set { self[SnapChefThemeKey.self] = newValue }
    }
}

struct SnapChefTokens {
    // Colors
    let primary            = Color.greenPrimary
    let onPrimary          = Color.greenOnPrimary
    let primaryContainer   = Color.greenSecondary
    let onPrimaryContainer = Color.greenOnSecondary
    let secondary          = Color.greenSecondary
    let onSecondary        = Color.greenOnSecondary
    let background         = Color.greenBackground
    let onBackground       = Color.greenOnBackground
    let surface            = Color.greenSurface
    let onSurface          = Color.greenOnSurface
    let error              = Color.greenError

    // Typography
    let displayLarge   = Font.snapDisplayLarge
    let headlineMedium = Font.snapHeadlineMedium
    let titleLarge     = Font.snapTitleLarge
    let bodyLarge      = Font.snapBodyLarge
    let bodyMedium     = Font.snapBodyMedium
    let labelLarge     = Font.snapLabelLarge
}

struct SnapChefTheme<Content: View>: View {
    @ViewBuilder let content: () -> Content

    var body: some View {
        content()
            .environment(\.snapTheme, SnapChefTokens())
            .background(Color.greenBackground)
    }
}
