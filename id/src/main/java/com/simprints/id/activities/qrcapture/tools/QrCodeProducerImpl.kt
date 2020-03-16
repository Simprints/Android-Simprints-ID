package com.simprints.id.activities.qrcapture.tools

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysisConfig
import androidx.camera.core.ImageProxy
import com.simprints.id.activities.qrcapture.model.RawImage
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

@ExperimentalCoroutinesApi
class QrCodeProducerImpl(
    private val qrCodeDetector: QrCodeDetector,
    private val crashReportManager: CrashReportManager
) : QrCodeProducer, ImageAnalysis.Analyzer {

    private val analysisConfig by lazy {
        ImageAnalysisConfig.Builder()
            .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            .build()
    }

    override val useCase by lazy {
        ImageAnalysis(analysisConfig).apply {
            setAnalyzer(Executors.newSingleThreadExecutor(), this@QrCodeProducerImpl)
        }
    }

    override var qrCodeChannel = Channel<String>(Channel.CONFLATED)

    override fun analyze(imageProxy: ImageProxy?, rotationDegrees: Int) {
        imageProxy?.image?.let { mediaImage ->
            runBlocking {
                try {
                    val image = RawImage(mediaImage, rotationDegrees)
                    qrCodeDetector.detectInImage(image)?.let { qrCode ->
                        if (!qrCodeChannel.isClosedForSend) {
                            with(qrCodeChannel) {
                                send(qrCode)
                                close()
                            }
                        }
                    }
                } catch (t: Throwable) {
                    crashReportManager.logExceptionOrSafeException(t)
                }
            }
        }
    }

}
