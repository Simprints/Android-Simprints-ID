package com.simprints.id.activities.qrcapture.tools

import androidx.camera.core.*
import java.util.concurrent.Executors

class QrCaptureHelperImpl(private val qrCodeAnalyser: QrCodeAnalyser) : QrCaptureHelper {

    override fun buildPreview(): Preview {
        val previewConfig = PreviewConfig.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()

        return Preview(previewConfig)
    }

    override fun buildUseCase(qrCaptureListener: QrCaptureListener): ImageAnalysis {
        qrCodeAnalyser.setQrCaptureListener(qrCaptureListener)

        val analysisConfig = ImageAnalysisConfig.Builder()
            .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            .build()

        return ImageAnalysis(analysisConfig).also { analysis ->
            analysis.setAnalyzer(Executors.newSingleThreadExecutor(), qrCodeAnalyser)
        }
    }

}
