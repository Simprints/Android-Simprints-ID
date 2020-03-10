package com.simprints.id.activities.qrcapture.tools

import android.media.Image
import androidx.camera.core.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

class QrCodeProducerImpl(private var qrCodeDetector: QrCodeDetector) : QrCodeProducer, ImageAnalysis.Analyzer {

    private val analysisConfig by lazy {
        ImageAnalysisConfig.Builder()
            .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            .build()
    }

    override val imageAnalyser: UseCase by lazy {
        ImageAnalysis(analysisConfig).apply {
            setAnalyzer(Executors.newSingleThreadExecutor(), this@QrCodeProducerImpl)
        }
    }

    override val qrCodeChannel = Channel<String>(Channel.CONFLATED)

    override fun analyze(imageProxy: ImageProxy?, rotationDegrees: Int) {
        imageProxy?.image?.let { mediaImage ->
            runBlocking {
                try {
                    val qrCode = qrCodeDetector.detectInImage(RawImage(mediaImage, rotationDegrees))
                    qrCode?.let {
                        if (!qrCodeChannel.isClosedForSend) {
                            qrCodeChannel.send(it)
                            qrCodeChannel.close()
                        }
                    }
                } catch (t: Throwable) {
                    t.printStackTrace()
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
