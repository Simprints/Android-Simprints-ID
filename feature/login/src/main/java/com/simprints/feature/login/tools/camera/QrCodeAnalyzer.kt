package com.simprints.feature.login.tools.camera

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.simprints.core.DispatcherBG
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.LOGIN
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

internal class QrCodeAnalyzer @Inject constructor(
    private val qrCodeDetector: QrCodeDetector,
    @DispatcherBG private val bgDispatcher: CoroutineDispatcher,
) : ImageAnalysis.Analyzer {
    private val _scannedCode = MutableStateFlow<String?>(null)
    val scannedCode: Flow<String> = _scannedCode
        .filterNotNull()
        .buffer(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.use {
            it.image?.let { mediaImage ->
                runBlocking(bgDispatcher) {
                    try {
                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                        val image = RawImage(mediaImage, rotationDegrees)
                        qrCodeDetector.detectInImage(image)?.let(_scannedCode::tryEmit)
                    } catch (t: Throwable) {
                        Simber.e("QR code detection failed", t, tag = LOGIN)
                    }
                }
            }
        }
    }
}
