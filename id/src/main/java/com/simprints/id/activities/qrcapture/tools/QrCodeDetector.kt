package com.simprints.id.activities.qrcapture.tools

import com.simprints.id.activities.qrcapture.model.RawImage

interface QrCodeDetector {
    suspend fun detectInImage(rawImage: RawImage): String?
}
