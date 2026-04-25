
import Foundation
import Shared

@MainActor
final class VerificationViewModel: ObservableObject {

    @Published var code:         String  = ""
    @Published var isLoading:    Bool    = false
    @Published var errorMessage: String? = nil

    private let apiService = SnapChefServiceLocator.shared.authApiService

    func updateCode(_ newCode: String) {
        guard newCode.count <= 6 else { return }
        code         = newCode
        errorMessage = nil
    }

    func verify(
        email:     String,
        onSuccess: @escaping () -> Void
    ) {
        guard code.count == 6 else {
            errorMessage = "Please enter the 6-digit code."
            return
        }

        isLoading    = true
        errorMessage = nil

        Task {
            do {
                let response = try await apiService.verify(
                    request: VerifyRequest(email: email, code: code)
                )
                AuthManager.shared.accessToken = response.accessToken
                AuthManager.shared.currentUser = response.user
                onSuccess()
            } catch {
                isLoading    = false
                errorMessage = "Verification failed. The code might be invalid or expired."
            }
        }
    }
}
