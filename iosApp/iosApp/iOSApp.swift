import SwiftUI
import GoogleSignIn

@main
struct iOSApp: App {

    init() {
        let clientID = Bundle.main.object(forInfoDictionaryKey: "GIDClientID") as? String ?? ""
        let serverClientID = Bundle.main.object(forInfoDictionaryKey: "GIDServerClientID") as? String ?? ""
        
        if clientID.isEmpty {
            print("GIDClientID missing from Info.plist")
        }
        
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(
            clientID: clientID,
            serverClientID: serverClientID
        )
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
