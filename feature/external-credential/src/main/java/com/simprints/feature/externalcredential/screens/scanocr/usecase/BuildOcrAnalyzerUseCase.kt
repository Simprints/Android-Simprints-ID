package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import androidx.camera.core.ImageInfo
import androidx.camera.core.ImageProxy
import javax.inject.Inject

internal class CaptureFrameUseCase @Inject constructor() {

    suspend operator fun invoke(imageProxy: ImageProxy): Pair<Bitmap, ImageInfo>? {
        // Converting ImageProxy to Bitmap so that we don't forget to close the ImageProxy down the processing chain
        val bitmapWithImageInfo = imageProxy.toBitmap() to imageProxy.imageInfo
        return bitmapWithImageInfo
    }
}
