
import Foundation

@MainActor
final class WelcomeViewModel: ObservableObject {

    @Published var title:    String = "SnapChef"
    @Published var subtitle: String = "Snap a photo, discover a recipe.\nCook smarter every day."

    func applyBackendJson(_ json: String) {
        guard
            let data = json.data(using: .utf8),
            let obj  = try? JSONSerialization.jsonObject(with: data) as? [String: Any]
        else { return }

        if let t = obj["title"]    as? String { title    = t }
        if let s = obj["subtitle"] as? String { subtitle = s }
    }
}
