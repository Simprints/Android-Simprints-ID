package com.simprints.infra.camera.usecase

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import androidx.core.graphics.createBitmap
import javax.inject.Inject

internal class InjectedImagePreProcessUseCase @Inject constructor() {
    /**
     * Fits [injectedImage] centred within [targetRect], preserving aspect ratio, and expanded to [previewRect]. The remaining area is
     * filled with black color. This is done so that the injected image is properly displayed on the screen, and post-processing can
     * crop the area of interest as if it was a real frame from the camera.
     */
    operator fun invoke(
        injectedImage: Bitmap,
        previewRect: Rect,
        targetRect: Rect,
    ): Bitmap {
        val result = createBitmap(previewRect.width(), previewRect.height())
        val canvas = Canvas(result)
        canvas.drawColor(Color.BLACK)

        val scale = if (targetRect.width() > targetRect.height()) {
            targetRect.height().toFloat() / injectedImage.height
        } else {
            targetRect.width().toFloat() / injectedImage.width
        }

        val scaledWidth = (injectedImage.width * scale).toInt()
        val scaledHeight = (injectedImage.height * scale).toInt()

        val left = targetRect.left + (targetRect.width() - scaledWidth) / 2
        val top = targetRect.top + (targetRect.height() - scaledHeight) / 2

        canvas.drawBitmap(injectedImage, null, Rect(left, top, left + scaledWidth, top + scaledHeight), null)

        return result
    }
}
