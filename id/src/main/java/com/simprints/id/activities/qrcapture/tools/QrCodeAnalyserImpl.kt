package com.simprints.id.activities.qrcapture.tools

import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata

class QrCodeAnalyserImpl(private val qrCodeDetector: QrCodeDetector) : QrCodeAnalyser {

    private var listener: QrCaptureListener? = null

    override fun setQrCaptureListener(listener: QrCaptureListener) {
        this.listener = listener
    }

    override fun analyze(imageProxy: ImageProxy?, rotationDegrees: Int) {
        imageProxy?.image?.let { mediaImage ->
            val rotation = degreesToFirebaseRotation(rotationDegrees)
            val image = FirebaseVisionImage.fromMediaImage(mediaImage, rotation)
            listener?.let {
                qrCodeDetector.detectInImage(image, it)
            }
        }
    }

    private fun degreesToFirebaseRotation(degrees: Int): Int = when (degrees) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Throwable("Rotation must be 0, 90, 180 or 270")
    }

}
