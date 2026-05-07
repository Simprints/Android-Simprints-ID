package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import android.graphics.Rect
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
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

        return if (width <= 0 || height <= 0) {
            val message =
                "Invalid OCR crop dimensions: width=$width, height=$height. CutoutRect=[$cutoutRect], bitmapSize(w,h)=[${bitmap.width}x${bitmap.height}]"
            Simber.e(message = message, t = IllegalStateException(message), tag = MULTI_FACTOR_ID)
            // Returning original bitmap without zooming in
            return bitmap
        } else {
            Bitmap.createBitmap(bitmap, left, top, width, height)
        }
    }
}
