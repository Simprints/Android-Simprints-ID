package com.simprints.infra.uibase.camera.qrscan

import android.graphics.Rect
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.simprints.core.DispatcherBG
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.camera.qrscan.usecase.CropBitmapAreaForDetectionUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.runBlocking


class QrCodeAnalyzer @AssistedInject constructor(
    private val qrCodeDetectorFactory: QrCodeDetector.Factory,
    @DispatcherBG private val bgDispatcher: CoroutineDispatcher,
    @Assisted private val cropConfig: CropConfig?,
    @Assisted private val crashReportTag: LoggingConstants.CrashReportTag,
    private val cropBitmapAreaForDetectionUseCase: CropBitmapAreaForDetectionUseCase
) : ImageAnalysis.Analyzer {


    @AssistedFactory
    interface Factory {
        fun create(cropConfig: CropConfig?, crashReportTag: LoggingConstants.CrashReportTag): QrCodeAnalyzer
    }

    data class CropConfig(
        val rect: Rect,
        val orientation: Int,
        val rootViewWidth: Int,
        val rootViewHeight: Int,
    )

    private val qrCodeDetector by lazy { qrCodeDetectorFactory.create(crashReportTag) }
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
                        when (cropConfig) {
                            null -> {
                                val image = RawImage(mediaImage, rotationDegrees)
                                qrCodeDetector.detectInImage(image)?.let(_scannedCode::tryEmit)
                            }

                            else -> {
                                val bitmap = cropBitmapAreaForDetectionUseCase(imageProxy.toBitmap(), cropConfig)
                                val image = InputImage.fromBitmap(bitmap, 0)
                                qrCodeDetector.detectInImage(image)?.let(_scannedCode::tryEmit)
                            }
                        }

                    } catch (t: Throwable) {
                        Simber.e("QR code detection failed", t, tag = crashReportTag)
                    }
                }
            }
        }
    }
}
