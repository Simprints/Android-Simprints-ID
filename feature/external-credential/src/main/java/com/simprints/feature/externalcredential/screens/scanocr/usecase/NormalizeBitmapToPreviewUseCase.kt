package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import android.graphics.Matrix
import javax.inject.Inject
import androidx.core.graphics.scale
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrCropConfig

internal class NormalizeBitmapToPreviewUseCase @Inject constructor() {

    /**
     * Normalizes a camera capture [inputBitmap] bitmap to match the PreviewView's dimensions and aspect ratio.
     *
     * This method performs three transformations:
     * 1. Rotation - Rotates the bitmap by the specified degrees if needed
     * 2. Center cropping - Crops the bitmap to match PreviewView aspect ratio, keeping the center portion
     * 3. Scaling - Scales the cropped bitmap to exactly match PreviewView dimensions
     *
     * The center cropping ensures that the normalized bitmap has the same aspect ratio as what the user
     * sees in the camera preview, making OCR results spatially consistent with the preview overlay.
     *
     * @param inputBitmap the original camera capture bitmap
     * @param cropConfig configuration containing rotation, preview width and height
     *
     * @return normalized bitmap with PreviewView dimensions and aspect ratio
     */
    suspend operator fun invoke(
        inputBitmap: Bitmap,
        cropConfig: OcrCropConfig
    ): Bitmap {
        val rotationDegrees = cropConfig.rotationDegrees
        val previewViewWidth = cropConfig.previewViewWidth
        val previewViewHeight = cropConfig.previewViewHeight

        // Rotate if necessary
        val rotated = if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(inputBitmap, 0, 0, inputBitmap.width, inputBitmap.height, matrix, true)
        } else inputBitmap

        // Center-crop to match PreviewView aspect ratio
        val previewRatio = previewViewWidth.toFloat() / previewViewHeight
        val inputRatio = rotated.width.toFloat() / rotated.height

        val cropWidth: Int
        val cropHeight: Int
        val offsetX: Int
        val offsetY: Int

        if (inputRatio > previewRatio) {
            cropHeight = rotated.height
            cropWidth = (cropHeight * previewRatio).toInt()
            offsetX = (rotated.width - cropWidth) / 2
            offsetY = 0
        } else {
            cropWidth = rotated.width
            cropHeight = (cropWidth / previewRatio).toInt()
            offsetX = 0
            offsetY = (rotated.height - cropHeight) / 2
        }

        val cropped = Bitmap.createBitmap(rotated, offsetX, offsetY, cropWidth, cropHeight)

        // Scale to PreviewView size
        return cropped.scale(previewViewWidth, previewViewHeight)
    }
}

