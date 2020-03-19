package com.simprints.id.activities.qrcapture.tools

import androidx.camera.core.AspectRatio
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
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

    override val useCase by lazy {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build().apply {
                setAnalyzer(Executors.newSingleThreadExecutor(), this@QrCodeProducerImpl)
            }
    }

    override var qrCodeChannel = Channel<String>(Channel.CONFLATED)

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let { mediaImage ->
            runBlocking {
                try {
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
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
                } finally {
                    imageProxy.close()
                }
            }
        }
    }

}
