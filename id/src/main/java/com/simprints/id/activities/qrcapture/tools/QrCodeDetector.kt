package com.simprints.id.activities.qrcapture.tools

interface QrCodeDetector {
    suspend fun detectInImage(rawImage: RawImage): String?
}
