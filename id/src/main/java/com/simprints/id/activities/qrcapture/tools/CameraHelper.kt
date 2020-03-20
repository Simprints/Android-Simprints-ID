package com.simprints.id.activities.qrcapture.tools

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner

interface CameraHelper {

    suspend fun startCamera(
        lifecycleOwner: LifecycleOwner,
        cameraPreview: PreviewView,
        qrCodeProducer: QrCodeProducer
    )

}
