package com.simprints.id.activities.qrcapture.tools

import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.simprints.id.activities.qrcapture.model.RawImage
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.tools.extensions.awaitTask

class QrCodeDetectorImpl(private val crashReportManager: CrashReportManager) : QrCodeDetector {

    override suspend fun detectInImage(rawImage: RawImage): String? {
        val firebaseRotation = degreesToFirebaseRotation(rawImage.rotationDegrees)
        val firebaseImage = FirebaseVisionImage.fromMediaImage(rawImage.image, firebaseRotation)
        val firebaseDetector = buildFirebaseDetector()

        return try {
            val qrCodes = firebaseDetector.detectInImage(firebaseImage).awaitTask()
            return qrCodes?.firstOrNull { !it.rawValue.isNullOrEmpty() }?.rawValue
        } catch (t: Throwable) {
            crashReportManager.logExceptionOrSafeException(t)
            null
        }
    }

    private fun degreesToFirebaseRotation(degrees: Int): Int = when (degrees) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw IllegalArgumentException("Rotation must be 0, 90, 180 or 270")
    }

    private fun buildFirebaseDetector(): FirebaseVisionBarcodeDetector {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
            .build()

        return FirebaseVision.getInstance().getVisionBarcodeDetector(options)
    }

}
