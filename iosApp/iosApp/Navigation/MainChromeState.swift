import SwiftUI

@MainActor
final class MainChromeState: ObservableObject {
    @Published var hideTabBar: Bool = false
}
