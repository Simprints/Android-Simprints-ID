package com.simprints.infra.uibase.camera.qrscan

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
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

class QrCodeAnalyzer @Inject constructor(
    private val qrCodeDetector: QrCodeDetector,
    @DispatcherBG private val bgDispatcher: CoroutineDispatcher,
) : ImageAnalysis.Analyzer {
    private val _scannedCode = MutableStateFlow<String?>(null)
    val scannedCode: Flow<String> = _scannedCode
        .filterNotNull()
        .buffer(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    var getRect: () -> Rect? = { null }
    var getOrientation: () -> Int? = { null }
    var getViewSize: () -> (Pair<Int?, Int?>) = {
        Pair(null, null)
    }

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.use {
            it.image?.let { mediaImage ->
                runBlocking(bgDispatcher) {
                    try {
                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                        when (val cropConfig = getCropConfig()) {
                            null -> {
                                val image = RawImage(mediaImage, rotationDegrees)
                                qrCodeDetector.detectInImage(image)?.let(_scannedCode::tryEmit)
                            }

                            else -> {
                                val bitmap = getCroppedBitmap(imageProxy.toBitmap(), cropConfig)
                                val image = InputImage.fromBitmap(bitmap, 0)
                                qrCodeDetector.detectInImage(image)?.let(_scannedCode::tryEmit)
                            }
                        }

                    } catch (t: Throwable) {
                        Simber.e("QR code detection failed", t, tag = LOGIN)
                    }
                }
            }
        }
    }


    private data class CropConfig(
        val rect: Rect,
        val orientation: Int,
        val viewHeight: Int,
        val viewWidth: Int
    )

    private fun cropBitmapToRect(source: Bitmap, cropRect: Rect): Bitmap {
        val left = cropRect.left.coerceIn(0, source.width)
        val top = cropRect.top.coerceIn(0, source.height)
        val width = cropRect.width().coerceAtMost(source.width - left)
        val height = cropRect.height().coerceAtMost(source.height - top)

        return Bitmap.createBitmap(source, left, top, width, height)
    }

    private fun mapCropRectToImageSpace(
        cropRectInRoot: Rect,
        rootWidth: Int,
        rootHeight: Int,
        imageWidth: Int,
        imageHeight: Int
    ): Rect {
        val scaleX = imageWidth.toFloat() / rootWidth
        val scaleY = imageHeight.toFloat() / rootHeight

        return Rect(
            (cropRectInRoot.left * scaleX).toInt(),
            (cropRectInRoot.top * scaleY).toInt(),
            (cropRectInRoot.right * scaleX).toInt(),
            (cropRectInRoot.bottom * scaleY).toInt()
        )
    }

    private fun rotateIfNeeded(bitmap: Bitmap, orientation: Int): Bitmap {
        val isLandscape = bitmap.width > bitmap.height
        val isPortraitOrientation = orientation == Configuration.ORIENTATION_PORTRAIT

        return if (isLandscape && isPortraitOrientation) {
            val matrix = Matrix().apply { postRotate(90f) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }

    private fun getCropConfig(): CropConfig? {
        val cropRect = getRect()
        val orientation = getOrientation()
        val rootSize = getViewSize()
        if (cropRect != null && orientation != null && rootSize.first != null && rootSize.second != null) {
            return CropConfig(
                rect = cropRect,
                orientation = orientation,
                viewWidth = rootSize.first!!,
                viewHeight = rootSize.second!!
            )
        } else return null
    }

    private fun getCroppedBitmap(fullBitmap: Bitmap, cropConfig: CropConfig): Bitmap {
        val bitmap = rotateIfNeeded(fullBitmap, cropConfig.orientation)
        val crop = mapCropRectToImageSpace(
            cropRectInRoot = cropConfig.rect,
            rootWidth = cropConfig.viewWidth,
            rootHeight = cropConfig.viewHeight,
            imageWidth = bitmap.width,
            imageHeight = bitmap.height
        )
        return if (crop.left < 0 || crop.top < 0 || crop.right > bitmap.width || crop.bottom > bitmap.height) {
            fullBitmap
        } else {
            cropBitmapToRect(bitmap, crop)
        }
    }
}
