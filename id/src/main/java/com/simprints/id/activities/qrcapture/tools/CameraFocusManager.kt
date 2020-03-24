package com.simprints.id.activities.qrcapture.tools

import androidx.camera.core.Camera
import androidx.camera.view.PreviewView

interface CameraFocusManager {
    fun setUpFocusOnTap(cameraPreview: PreviewView, camera: Camera)
    fun setUpAutoFocus(cameraPreview: PreviewView, camera: Camera)
}
