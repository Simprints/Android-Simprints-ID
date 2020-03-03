package com.simprints.id.activities.qrcapture.tools

import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysisConfig
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import java.util.concurrent.Executors

class QrCaptureHelperImpl : QrCaptureHelper, OnSuccessListener<List<FirebaseVisionBarcode>> {

    private lateinit var qrCaptureListener: QrCaptureListener

    override fun buildPreview(): Preview {
        val resolution = Size(1280, 720)
        val previewConfig = PreviewConfig.Builder()
            .setTargetResolution(resolution)
            .build()

        return Preview(previewConfig)
    }

    override fun buildUseCase(qrCaptureListener: QrCaptureListener): ImageAnalysis {
        this.qrCaptureListener = qrCaptureListener

        val analysisConfig = ImageAnalysisConfig.Builder()
            .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            .build()

        val analyser = QrCodeAnalyser()
            .also { it.setOnSuccessListener(this) }

        return ImageAnalysis(analysisConfig).also { analysis ->
            analysis.setAnalyzer(Executors.newSingleThreadExecutor(), analyser)
        }
    }

    override fun onSuccess(qrCodes: List<FirebaseVisionBarcode>?) {
        if (!qrCodes.isNullOrEmpty()) {
            val qrCode = qrCodes.first { !it.rawValue.isNullOrEmpty() }
            qrCode.rawValue?.let {
                qrCaptureListener.onQrCodeCaptured(it)
            }
        }
    }

}
