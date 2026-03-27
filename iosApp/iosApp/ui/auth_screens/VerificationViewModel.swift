
import Foundation
import Shared

@MainActor
class VerificationViewModel: ObservableObject {
    @Published var code: String = ""
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil
    
    var onBack: (() -> Void)?
    var onSuccess: (() -> Void)?
    
    func updateCode(_ newCode: String) {
        if newCode.count <= 6 {
            code = newCode
            errorMessage = nil
        }
    }
    
    func verify(email: String) {
        guard code.count == 6 else {
            errorMessage = "Please enter the 6-digit code."
            return
        }
        
        isLoading = true
        errorMessage = nil
        
        Task {
            do {
                let request = VerifyRequest(email: email, code: code)
                let response = try await SnapChefServiceLocator.shared.authApiService.verify(request: request)
                
                AuthManager.shared.accessToken = response.accessToken
                AuthManager.shared.currentUser = response.user
                
                DispatchQueue.main.async {
                    self.isLoading = false
                    self.onSuccess?()
                }
            } catch {
                DispatchQueue.main.async {
                    self.isLoading = false
                    self.errorMessage = "Verification failed. The code might be invalid or expired."
                    print("Error: \(error)")
                }
            }
        }
    }
}
