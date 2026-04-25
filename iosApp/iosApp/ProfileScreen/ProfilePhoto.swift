
import SwiftUI

struct ProfilePhoto: View {
    let imageUri: URL?
    let initials: String
    var onClick: (() -> Void)? = nil

    @State private var loadedImage: UIImage? = nil

    var body: some View {
        Group {
            if let image = loadedImage {
                Image(uiImage: image)
                    .resizable()
                    .scaledToFill()
                    .clipShape(Circle())
            } else {
                ZStack {
                    Circle()
                        .fill(Color.greenSecondary)

                    Text(initials)
                        .font(.system(size: 36, weight: .bold))
                        .foregroundColor(Color.greenOnBackground)
                }
            }
        }
        .onTapGesture {
            onClick?()
        }
        .task(id: imageUri) {
            guard let url = imageUri else { return }
            loadedImage = await loadImage(from: url)
        }
    }

    private func loadImage(from url: URL) async -> UIImage? {
        if url.isFileURL {
            return UIImage(contentsOfFile: url.path)
        }
        guard let (data, _) = try? await URLSession.shared.data(from: url) else { return nil }
        return UIImage(data: data)
    }
}
