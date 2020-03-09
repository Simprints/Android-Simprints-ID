package com.simprints.id.activities.qrcapture.tools

import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage

class QrCodeDetectorImpl : QrCodeDetector {

    override fun detectInImage(image: FirebaseVisionImage, qrCaptureListener: QrCaptureListener) {
        val firebaseDetector = buildFirebaseDetector()

        firebaseDetector.detectInImage(image).addOnSuccessListener { qrCodes ->
            if (!qrCodes.isNullOrEmpty()) {
                val qrCode = qrCodes.first { !it.rawValue.isNullOrEmpty() }
                qrCode.rawValue?.let(qrCaptureListener::onQrCodeCaptured)
            }
        }
    }

    private fun buildFirebaseDetector(): FirebaseVisionBarcodeDetector {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
            .build()

        return FirebaseVision.getInstance().getVisionBarcodeDetector(options)
    }

}
