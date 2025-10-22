package com.simprints.infra.uibase.camera.qrscan.usecase

import android.graphics.Bitmap
import android.graphics.Rect
import javax.inject.Inject

class CropBitmapToRectUseCase @Inject constructor() {
    /**
     * Crops given [source] bitmap to the specified [cropRect]
     *
     * @param source bitmap to crop
     * @param cropRect target crop rectangle
     * @return a new [Bitmap] representing the cropped region
     */
    operator fun invoke(source: Bitmap, cropRect: Rect): Bitmap {
        val left = cropRect.left.coerceIn(0, source.width)
        val top = cropRect.top.coerceIn(0, source.height)
        val width = cropRect.width().coerceAtMost(source.width - left)
        val height = cropRect.height().coerceAtMost(source.height - top)

        return Bitmap.createBitmap(source, left, top, width, height)
    }
}
