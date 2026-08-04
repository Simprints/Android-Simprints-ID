package com.simprints.face.capture.usecases

import android.graphics.Bitmap
import android.graphics.RectF
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

internal class CropBitmapToOverlayUseCase @Inject constructor() {
    operator fun invoke(
        bitmap: Bitmap,
        previewRect: RectF,
        overlayWidth: Int,
        overlayHeight: Int,
    ): Bitmap {
        if (previewRect.isEmpty || overlayWidth == 0 || overlayHeight == 0) return bitmap

        val scale = min(
            bitmap.width / overlayWidth.toFloat(),
            bitmap.height / overlayHeight.toFloat(),
        )
        val scaledWidth = (overlayWidth * scale).toInt()
        val scaledHeight = (overlayHeight * scale).toInt()
        val offsetX = (max(bitmap.width, scaledWidth) - min(bitmap.width, scaledWidth)) / 2
        val offsetY = (max(bitmap.height, scaledHeight) - min(bitmap.height, scaledHeight)) / 2

        val cropLeft = offsetX + (previewRect.left * scale).toInt()
        val cropTop = offsetY + (previewRect.top * scale).toInt()
        val cropWidth = (previewRect.width() * scale).toInt()
        val cropHeight = (previewRect.height() * scale).toInt()

        if (cropLeft < 0 || cropTop < 0 ||
            cropWidth <= 0 || cropHeight <= 0 ||
            cropLeft + cropWidth > bitmap.width ||
            cropTop + cropHeight > bitmap.height
        ) {
            return bitmap
        }

        return Bitmap.createBitmap(bitmap, cropLeft, cropTop, cropWidth, cropHeight)
    }
}
