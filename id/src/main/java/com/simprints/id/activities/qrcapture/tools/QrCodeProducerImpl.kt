package com.simprints.id.activities.qrcapture.tools

import android.media.Image
import androidx.camera.core.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

class QrCodeProducerImpl(var qrCodeDetector: QrCodeDetector) : QrCodeProducer, ImageAnalysis.Analyzer {

    private val analysisConfig = ImageAnalysisConfig.Builder()
        .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        .build()

    override val imageAnalyser = ImageAnalysis(analysisConfig).also { analysis ->
        analysis.setAnalyzer(Executors.newSingleThreadExecutor(), this)
    }

    override val qrCodeChannel = Channel<String>(Channel.CONFLATED)

    override fun analyze(imageProxy: ImageProxy?, rotationDegrees: Int) {
        imageProxy?.image?.let { mediaImage ->
            runBlocking {
                val qrCode = qrCodeDetector.detectInImage(RawImage(mediaImage, rotationDegrees))
                qrCode?.let {
                    if (!qrCodeChannel.isClosedForSend) {
                        qrCodeChannel.send(it)
                        qrCodeChannel.close()
                    }
                }
            }
        }
    }
}

data class RawImage(val image: Image, val rotationDegrees: Int)

class QrPreviewBuilder {
    fun buildPreview(): Preview {
        val previewConfig = PreviewConfig.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()

        return Preview(previewConfig)
    }
}
