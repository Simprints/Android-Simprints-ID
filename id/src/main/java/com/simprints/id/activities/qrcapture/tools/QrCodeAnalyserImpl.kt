package com.simprints.id.activities.qrcapture.tools

import androidx.camera.core.ImageProxy

class QrCodeAnalyserImpl(private val qrCodeDetector: QrCodeDetector) : QrCodeAnalyser {

    private var listener: QrCaptureListener? = null

    override fun setQrCaptureListener(listener: QrCaptureListener) {
        this.listener = listener
    }

    override fun analyze(imageProxy: ImageProxy?, rotationDegrees: Int) {
        imageProxy?.image?.let { mediaImage ->
            listener?.let {
                qrCodeDetector.detectInImage(mediaImage, rotationDegrees, it)
            }
        }
    }

}
