package com.simprints.id.activities.qrcapture.tools

import androidx.camera.core.ImageAnalysis

interface QrCodeAnalyser : ImageAnalysis.Analyzer {
    fun setQrCaptureListener(listener: QrCaptureListener)
}
