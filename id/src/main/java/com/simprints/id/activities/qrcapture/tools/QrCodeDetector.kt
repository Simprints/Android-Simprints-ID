package com.simprints.id.activities.qrcapture.tools

import com.google.firebase.ml.vision.common.FirebaseVisionImage

interface QrCodeDetector {
    fun detectInImage(image: FirebaseVisionImage, qrCaptureListener: QrCaptureListener)
}
