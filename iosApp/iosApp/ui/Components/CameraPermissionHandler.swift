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

    @Published var showCameraDeniedAlert = false
    @Published var showPhotosDeniedAlert = false

    // Camera only

    func requestCameraPermission(onGranted: @escaping () -> Void) {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            cameraStatus = .granted
            onGranted()
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { granted in
                DispatchQueue.main.async {
                    self.cameraStatus = granted ? .granted : .denied
                    if granted { onGranted() } else { self.showCameraDeniedAlert = true }
                }
            }
        default:
            cameraStatus = .denied
            showCameraDeniedAlert = true
        }
    }

    // Photos only

    func requestPhotosPermission(onGranted: @escaping () -> Void) {
        switch PHPhotoLibrary.authorizationStatus(for: .readWrite) {
        case .authorized, .limited:
            photoStatus = .granted
            onGranted()
        case .notDetermined:
            PHPhotoLibrary.requestAuthorization(for: .readWrite) { status in
                DispatchQueue.main.async {
                    let granted = status == .authorized || status == .limited
                    self.photoStatus = granted ? .granted : .denied
                    if granted { onGranted() } else { self.showPhotosDeniedAlert = true }
                }
            }
        default:
            photoStatus = .denied
            showPhotosDeniedAlert = true
        }
    }
    
    //Both
    func requestBothPermissions(onGranted: @escaping () -> Void) {
        requestCameraPermission {
            self.requestPhotosPermission {
                onGranted()
            }
        }
    }
}
