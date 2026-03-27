//
//  GoogleAuthHelper.swift
//  iosApp
//
//  Created by gergana on 3/28/26.
//

import GoogleSignIn
import UIKit

enum GoogleAuthHelper {
    
    static func signInWithGoogle(context: AnyObject) async -> String? {
        guard
            let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
            let root  = scene.windows.first?.rootViewController
        else { return nil }
        
        do {
            let result = try await GIDSignIn.sharedInstance.signIn(withPresenting: root)
            return result.user.idToken?.tokenString
        } catch {
            print("Google Sign-In error: \(error.localizedDescription)")
            return nil
        }
    }
}
