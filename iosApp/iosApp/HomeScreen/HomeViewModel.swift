import SwiftUI
import Combine
import Shared

@MainActor
final class HomeViewModel: ObservableObject {
    @Published var isAnalyzing:  Bool       = false
    @Published var ingredients:  [String]   = []
    @Published var errorMessage: String?    = nil
    @Published var sessionId:    Int32?     = nil
    
    private let apiService = SnapChefServiceLocator.shared.homeApiService

    func analyzeImage(image: UIImage) {
        guard let resized = resizeImage(image, maxDimension: 1080),
              let jpegData = resized.jpegData(compressionQuality: 0.8) else {
            errorMessage = "Failed to process the image."
            return
        }
        
        isAnalyzing  = true
        errorMessage = nil
        ingredients  = []
        sessionId    = nil
        
        Task {
            do {
                let kotlinBytes = toKotlinByteArray(data: jpegData)
                let response = try await apiService.scanFridgeImages(imagesBytes: [kotlinBytes])
                
                if response.items.isEmpty {
                    errorMessage = "No ingredients found in the photo. Please try a clearer picture."
                    isAnalyzing = false
                    return
                }
                
                do {
                    _ = try await apiService.confirmSession(sessionId: response.id)
                } catch {
                    print("Session confirmation warning: \(error)")
                }
                
                ingredients = response.items.map { $0.name }
                sessionId   = response.id
            } catch {
                errorMessage = "Failed to analyze ingredients: \(error.localizedDescription)"
            }
            isAnalyzing = false
        }
    }
    
    
    private func toKotlinByteArray(data: Data) -> KotlinByteArray {
        let array = KotlinByteArray(size: Int32(data.count))
        for (index, byte) in data.enumerated() {
            array.set(index: Int32(index), value: Int8(bitPattern: byte))
        }
        return array
    }
    
    private func resizeImage(_ image: UIImage, maxDimension: CGFloat) -> UIImage? {
        let size = image.size
        let maxSide = max(size.width, size.height)
        
        if maxSide <= maxDimension {
            return image
        }
        
        let scale = maxDimension / maxSide
        let newSize = CGSize(width: size.width * scale, height: size.height * scale)
        
        UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
        image.draw(in: CGRect(origin: .zero, size: newSize))
        let resizedImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        return resizedImage
    }
}
