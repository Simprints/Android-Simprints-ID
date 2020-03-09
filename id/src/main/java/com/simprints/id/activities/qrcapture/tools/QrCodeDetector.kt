package com.simprints.id.activities.qrcapture.tools

import android.media.Image

interface QrCodeDetector {
    fun detectInImage(
        image: Image,
        rotation: Int,
        qrCaptureListener: QrCaptureListener
    )
}
