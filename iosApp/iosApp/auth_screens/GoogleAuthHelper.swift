//
//  GoogleAuthHelper.swift
//  iosApp
//
//  Created by gergana on 3/28/26.
//

import GoogleSignIn
import UIKit

enum GoogleAuthHelper {
    
    @MainActor
    static func signInWithGoogle(context: AnyObject) async -> String? {
        guard
            let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
            let root  = scene.windows.first?.rootViewController
        else { return nil }
        
        do {
            print("Google Sign-In: Attempting to disconnect previous session...")
            try? await GIDSignIn.sharedInstance.disconnect()
            
            print("Google Sign-In: Starting sign-in with root view controller...")
            let result = try await GIDSignIn.sharedInstance.signIn(withPresenting: root)
            
            let token = result.user.idToken?.tokenString
            print("Google Sign-In: Successfully obtained token: \(token?.prefix(20) ?? "nil")...")
            return token
        } catch {
            print("Google Sign-In ERROR details: \(error)")
            print("Google Sign-In error localized: \(error.localizedDescription)")
            return nil
        }
    }
}
