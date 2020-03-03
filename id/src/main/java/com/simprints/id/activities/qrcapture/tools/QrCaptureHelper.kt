package com.simprints.id.activities.qrcapture.tools

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview

interface QrCaptureHelper {
    fun buildPreview(): Preview
    fun buildUseCase(qrCaptureListener: QrCaptureListener): ImageAnalysis
}
