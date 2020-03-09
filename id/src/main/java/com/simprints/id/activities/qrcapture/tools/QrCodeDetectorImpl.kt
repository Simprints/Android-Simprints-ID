package com.simprints.id.activities.qrcapture.tools

import android.media.Image
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.simprints.id.data.analytics.crashreport.CrashReportManager

class QrCodeDetectorImpl(
    private val crashReportManager: CrashReportManager
) : QrCodeDetector {

    override fun detectInImage(
        image: Image,
        rotation: Int,
        qrCaptureListener: QrCaptureListener
    ) {
        val firebaseRotation = degreesToFirebaseRotation(rotation)
        val firebaseImage = FirebaseVisionImage.fromMediaImage(image, firebaseRotation)
        val firebaseDetector = buildFirebaseDetector()

        firebaseDetector.detectInImage(firebaseImage).addOnSuccessListener { qrCodes ->
            if (!qrCodes.isNullOrEmpty()) {
                val qrCode = qrCodes.first { !it.rawValue.isNullOrEmpty() }
                qrCode.rawValue?.let(qrCaptureListener::onQrCodeCaptured)
            }
        }.addOnFailureListener {
            crashReportManager.logExceptionOrSafeException(it)
        }
    }

    private fun degreesToFirebaseRotation(degrees: Int): Int = when (degrees) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Throwable("Rotation must be 0, 90, 180 or 270")
    }

    private fun buildFirebaseDetector(): FirebaseVisionBarcodeDetector {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
            .build()

        return FirebaseVision.getInstance().getVisionBarcodeDetector(options)
    }

}
