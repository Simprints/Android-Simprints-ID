package com.simprints.infra.camera.usecase

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.core.graphics.scale
import javax.inject.Inject

internal class NormalizeHighResBitmapToPreviewUseCase @Inject constructor() {
    /**
     * Normalizes a camera capture [originalBitmap] to match the PreviewView's dimensions and aspect ratio.
     *
     * This method performs three transformations:
     * 1. Rotation - Rotates the bitmap by the specified degrees if needed
     * 2. Center cropping - Crops the bitmap to match PreviewView aspect ratio, keeping the center portion
     * 3. Scaling - Scales the cropped bitmap to exactly match PreviewView dimensions
     *
     * The center cropping ensures that the normalized high-res bitmap has the same aspect ratio as what the user
     * sees in the camera preview, making the target bounds spatially consistent with the preview overlay.
     *
     * @param originalBitmap the original camera capture bitmap
     *
     * @return a new bitmap with normalized dimensions and aspect ratio
     */
    operator fun invoke(
        originalBitmap: Bitmap,
        rotationDegrees: Int,
        previewWidth: Int,
        previewHeight: Int,
    ): Bitmap {
        // Rotate if necessary
        val rotated = if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
        } else {
            originalBitmap
        }

        // Center-crop to match PreviewView aspect ratio
        val previewRatio = previewWidth.toFloat() / previewHeight
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
        return cropped.scale(previewWidth, previewHeight)
    }
}
