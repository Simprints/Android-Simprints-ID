package com.simprints.feature.externalcredential.screens.ocr.usecase

import android.graphics.Bitmap
import android.graphics.RectF
import javax.inject.Inject

internal class CropCardAreaUseCase @Inject constructor() {
    /**
     * Crops a region from a full-size camera bitmap based on the 'cutoutRect' area drawn in the preview view. This use case tries to match
     * the exact area that user sees on the preview screen with the resulting high-res image from the camera. Since the proportions and the
     * size of the image and the preview might be different, we need to know exactly which part of the image to crop.
     *
     * @param image The full camera image (e.g., 3000x4000)
     * @param previewViewWidthPx The width of the preview view as seen on screen in pixels
     * @param previewViewHeightPx The height of the preview view as seen on screen in pixels
     * @param cutoutRect The rectangle (in preview coordinates) indicating the area of the card
     *
     * @return A cropped bitmap matching the 'cutoutRect' preview
     */
    suspend operator fun invoke(
        image: Bitmap,
        previewViewWidthPx: Int,
        previewViewHeightPx: Int,
        cutoutRect: RectF
    ): Bitmap {
        val scaleX = image.width.toFloat() / previewViewWidthPx
        val scaleY = image.height.toFloat() / previewViewHeightPx

        val cropX = (cutoutRect.left * scaleX).toInt()
        val cropY = (cutoutRect.top * scaleY).toInt()
        val cropWidth = (cutoutRect.width() * scaleX).toInt()
        val cropHeight = (cutoutRect.height() * scaleY).toInt()

        val safeX = cropX.coerceAtLeast(0)
        val safeY = cropY.coerceAtLeast(0)
        val safeWidth = (safeX + cropWidth).coerceAtMost(image.width) - safeX
        val safeHeight = (safeY + cropHeight).coerceAtMost(image.height) - safeY

        return Bitmap.createBitmap(image, safeX, safeY, safeWidth, safeHeight)
    }
}
