package com.simprints.id.activities.qrcapture.tools


import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.simprints.id.activities.qrcapture.model.RawImage
import com.simprints.id.tools.extensions.awaitTask
import com.simprints.infra.logging.Simber
import javax.inject.Inject

class QrCodeDetectorImpl @Inject constructor(): QrCodeDetector {

    override suspend fun detectInImage(rawImage: RawImage): String? {
        val scanner = BarcodeScanning.getClient()

        return try {
            val qrCodes = scanner.process(InputImage.fromMediaImage(rawImage.image,rawImage.rotationDegrees)).awaitTask()
            return qrCodes?.firstOrNull { !it.rawValue.isNullOrEmpty() }?.rawValue
        } catch (t: Throwable) {
            Simber.e(t)
            null
        }
    }

}
