
import SwiftUI

struct RootNavigationView: View {
    @StateObject private var session = SessionManager.shared

    var body: some View {
        ZStack {
            if session.isLoggedIn {
                ScreenWrapper()
                    .transition(.opacity)
            } else {
                AuthView(onAuthSuccess: {
                    session.onAuthSuccess()
                })
                .transition(.opacity)
            }
        }
        .animation(.easeInOut(duration: 0.4), value: session.isLoggedIn)
        .environmentObject(session)
    }
}
