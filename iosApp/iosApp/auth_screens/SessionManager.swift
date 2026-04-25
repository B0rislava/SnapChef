
import SwiftUI
import Shared

@MainActor
final class SessionManager: ObservableObject {
    static let shared = SessionManager()

    @Published var isLoggedIn: Bool = AuthManager.shared.isLoggedIn()

    func logout() {
        AuthManager.shared.logout()
        isLoggedIn = false
    }

    func onAuthSuccess() {
        isLoggedIn = true
    }
}
