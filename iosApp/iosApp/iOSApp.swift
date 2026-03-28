import SwiftUI
import GoogleSignIn

@main
struct iOSApp: App {

    init() {
        guard let clientID = Bundle.main.object(forInfoDictionaryKey: "GIDSignIn") as? String else {
            print("GIDSignIn key missing from Info.plist")
            return
        }
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)
    }

    var body: some Scene {
        WindowGroup {
            SnapChefTheme {
                ContentView()
            }
            .onOpenURL { url in
                GIDSignIn.sharedInstance.handle(url)
            }
        }
    }
}
