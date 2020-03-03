package com.simprints.id.activities.qrcapture.tools

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata

class QrCodeAnalyser : ImageAnalysis.Analyzer {

    private var listener: OnSuccessListener<List<FirebaseVisionBarcode>>? = null

    fun setOnSuccessListener(listener: OnSuccessListener<List<FirebaseVisionBarcode>>) {
        this.listener = listener
    }

    override fun analyze(imageProxy: ImageProxy?, rotationDegrees: Int) {
        imageProxy?.image?.let { mediaImage ->
            val rotation = degreesToFirebaseRotation(rotationDegrees)
            val image = FirebaseVisionImage.fromMediaImage(mediaImage, rotation)
            val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
                .build()
            val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)
            listener?.let(detector.detectInImage(image)::addOnSuccessListener)
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
