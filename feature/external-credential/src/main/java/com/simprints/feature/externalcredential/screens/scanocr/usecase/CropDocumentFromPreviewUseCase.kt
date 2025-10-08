package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import android.graphics.Rect
import javax.inject.Inject

internal class CropDocumentFromPreviewUseCase @Inject constructor() {
    operator fun invoke(
        bitmap: Bitmap,
        cutoutRect: Rect,
    ): Bitmap {
        val left = cutoutRect.left.coerceIn(0, bitmap.width)
        val top = cutoutRect.top.coerceIn(0, bitmap.height)
        val right = cutoutRect.right.coerceIn(left, bitmap.width)
        val bottom = cutoutRect.bottom.coerceIn(top, bitmap.height)

        val width = right - left
        val height = bottom - top

        if (width <= 0 || height <= 0) {
            throw IllegalStateException(
                "Invalid OCR crop dimensions: width=$width, height=$height. CutoutRect=[$cutoutRect], bitmapSize(w,h)=[${bitmap.width}x${bitmap.height}]",
            )
        }

        return Bitmap.createBitmap(bitmap, left, top, width, height)
    }
}
