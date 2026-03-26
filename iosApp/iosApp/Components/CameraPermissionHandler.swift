//
//  CameraPermissionHandler.swift
//  iosApp
//
//  Created by gergana on 3/26/26.
//

import AVFoundation
import Photos
import SwiftUI

enum PermissionStatus {
    case idle, granted, denied
}

@MainActor
final class CameraPermissionHandler: ObservableObject {
    @Published var cameraStatus: PermissionStatus = .idle
    @Published var photoStatus:  PermissionStatus = .idle
    @Published var showDeniedAlert = false

    var allGranted: Bool {
        cameraStatus == .granted && photoStatus == .granted
    }

    func requestPermissions(onGranted: @escaping () -> Void) {
        requestCamera {
            self.requestPhotos {
                if self.allGranted {
                    onGranted()
                } else {
                    self.showDeniedAlert = true
                }
            }
        }
    }

    private func requestCamera(completion: @escaping () -> Void) {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            cameraStatus = .granted
            completion()
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { granted in
                DispatchQueue.main.async {
                    self.cameraStatus = granted ? .granted : .denied
                    completion()
                }
            }
        default:
            cameraStatus = .denied
            completion()
        }
    }

    private func requestPhotos(completion: @escaping () -> Void) {
        switch PHPhotoLibrary.authorizationStatus(for: .readWrite) {
        case .authorized, .limited:
            photoStatus = .granted
            completion()
        case .notDetermined:
            PHPhotoLibrary.requestAuthorization(for: .readWrite) { status in
                DispatchQueue.main.async {
                    self.photoStatus = (status == .authorized || status == .limited) ? .granted : .denied
                    completion()
                }
            }
        default:
            photoStatus = .denied
            completion()
        }
    }
}
