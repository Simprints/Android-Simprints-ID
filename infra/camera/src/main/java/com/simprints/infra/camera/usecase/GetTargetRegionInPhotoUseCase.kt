package com.simprints.infra.camera.usecase

import android.graphics.Rect
import javax.inject.Inject
import kotlin.math.min

internal class GetTargetRegionInPhotoUseCase @Inject constructor() {
    /**
     * Maps [targetRect] (in preview coordinates) onto the raw, unrotated [photoWidth]x[photoHeight] photo.
     *
     * The preview shows the center of the upright photo, cropped to the preview's aspect ratio, so the window is
     * first placed on the upright photo and then turned back by [rotationDegrees] into raw photo coordinates.
     */
    operator fun invoke(
        photoWidth: Int,
        photoHeight: Int,
        rotationDegrees: Int,
        previewRect: Rect,
        targetRect: Rect,
    ): Rect {
        val sideways = rotationDegrees % 180 != 0
        val uprightWidth = if (sideways) photoHeight else photoWidth
        val uprightHeight = if (sideways) photoWidth else photoHeight

        // Photo pixels per preview pixel, and where the preview's visible area starts on the upright photo
        val scale = min(uprightWidth / previewRect.width().toFloat(), uprightHeight / previewRect.height().toFloat())
        val offsetX = (uprightWidth - previewRect.width() * scale) / 2
        val offsetY = (uprightHeight - previewRect.height() * scale) / 2

        val left = targetRect.left.coerceIn(0, previewRect.width())
        val top = targetRect.top.coerceIn(0, previewRect.height())
        val right = targetRect.right.coerceIn(left, previewRect.width())
        val bottom = targetRect.bottom.coerceIn(top, previewRect.height())
        val upright = Rect(
            (offsetX + left * scale).toInt(),
            (offsetY + top * scale).toInt(),
            (offsetX + right * scale).toInt(),
            (offsetY + bottom * scale).toInt(),
        )

        return when (rotationDegrees) {
            0 -> upright
            90 -> Rect(upright.top, uprightWidth - upright.right, upright.bottom, uprightWidth - upright.left)
            180 -> Rect(
                uprightWidth - upright.right,
                uprightHeight - upright.bottom,
                uprightWidth - upright.left,
                uprightHeight - upright.top,
            )
            270 -> Rect(uprightHeight - upright.bottom, upright.left, uprightHeight - upright.top, upright.right)
            else -> error("Unsupported rotation $rotationDegrees")
        }.apply { intersect(0, 0, photoWidth, photoHeight) }
    }
}
