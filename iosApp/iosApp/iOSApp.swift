import SwiftUI
import GoogleSignIn

@main
struct iOSApp: App {
    var body: some Scene {
            WindowGroup {
                SnapChefTheme {
                    ContentView()
                        .onOpenURL { url in
                            GIDSignIn.sharedInstance.handle(url)
                        }
                }
            }
        }
}
