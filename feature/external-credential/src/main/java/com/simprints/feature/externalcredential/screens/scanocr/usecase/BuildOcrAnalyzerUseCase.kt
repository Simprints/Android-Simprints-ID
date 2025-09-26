package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import androidx.camera.core.ImageInfo
import androidx.camera.core.ImageProxy
import javax.inject.Inject

internal class CaptureFrameUseCase @Inject constructor() {
    private var frameCounter = 0

    suspend operator fun invoke(imageProxy: ImageProxy, framesToSkip: Int): Pair<Bitmap, ImageInfo>? {
        frameCounter++
        if (frameCounter % framesToSkip != 0) {
            imageProxy.close()
            return null
        }

        // Converting ImageProxy to Bitmap so that we don't forget to close the ImageProxy down the processing chain
        val bitmapWithImageInfo = imageProxy.toBitmap() to imageProxy.imageInfo
        imageProxy.close()
        return bitmapWithImageInfo
    }
}
