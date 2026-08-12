package com.simprints.infra.camera.usecase

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import androidx.core.graphics.createBitmap
import com.simprints.infra.camera.Frame
import com.simprints.infra.camera.repository.InjectedImageCache
import javax.inject.Inject

internal class FramePreProcessUseCase @Inject constructor(
    private val injectedImageCache: InjectedImageCache,
) {
    /**
     * Wraps the provided parameters with a [Frame] object.
     *
     * If an injected image is available, it will be used instead of the provided [bitmap].
     * Fits injected image centred within [targetRect], preserving aspect ratio, and expanded to [previewRect]. The remaining area is
     * filled with black color. This is done so that the injected image is properly displayed on the screen, and post-processing can
     * crop the area of interest as if it was a real frame from the camera.
     */
    operator fun invoke(
        bitmap: Bitmap,
        rotation: Int,
        previewRect: Rect,
        targetRect: Rect,
    ): Frame {
        val injected = injectedImageCache.injectedImage ?: return Frame(
            bitmap = bitmap,
            rotation = rotation,
            previewBounds = previewRect,
            targetBounds = targetRect,
        )

        val result = createBitmap(previewRect.width(), previewRect.height())
        val canvas = Canvas(result)
        canvas.drawColor(Color.BLACK)

        val scale = if (targetRect.width() > targetRect.height()) {
            targetRect.height().toFloat() / injected.height
        } else {
            targetRect.width().toFloat() / injected.width
        }

        val scaledWidth = (injected.width * scale).toInt()
        val scaledHeight = (injected.height * scale).toInt()

        val left = targetRect.left + (targetRect.width() - scaledWidth) / 2
        val top = targetRect.top + (targetRect.height() - scaledHeight) / 2

        canvas.drawBitmap(
            injected,
            null,
            Rect(left, top, left + scaledWidth, top + scaledHeight),
            null,
        )

        return Frame(
            bitmap = result,
            rotation = 0,
            previewBounds = previewRect,
            targetBounds = targetRect,
            isInjected = true,
        )
    }
}
