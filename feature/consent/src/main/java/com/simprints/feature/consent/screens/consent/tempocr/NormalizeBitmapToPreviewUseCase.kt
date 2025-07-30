package com.simprints.feature.consent.screens.consent.tempocr

import android.graphics.Bitmap
import android.graphics.Matrix
import javax.inject.Inject
import androidx.core.graphics.scale

class NormalizeBitmapToPreviewUseCase @Inject constructor() {

    operator fun invoke(
        inputBitmap: Bitmap,
        rotationDegrees: Int,
        previewViewWidth: Int,
        previewViewHeight: Int
    ): Bitmap {
        // 1. Rotate
        val rotated = if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(inputBitmap, 0, 0, inputBitmap.width, inputBitmap.height, matrix, true)
        } else inputBitmap

        // 2. Center-crop to match PreviewView aspect ratio
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

        // 3. Scale to PreviewView size
        return cropped.scale(previewViewWidth, previewViewHeight)
    }
}
