package com.simprints.face.capture.usecases

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.scale
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Turns a face detection bounding box into the square region that is both drawn on the preview and
 * fed to the rest of the capture pipeline.
 *
 * The square is centred on the detection and its side is the longer side of the detection, but it
 * is always kept fully inside the frame: the side is first capped at the frame's shorter dimension
 * and the square is then shifted inwards until it fits.
 */
internal class CropToFaceSquareUseCase @Inject constructor() {
    /**
     * @param faceBox detection bounding box in frame pixels
     * @return the square to draw and crop, in frame pixels, or an empty rect if it cannot be built
     */
    fun squareFor(
        faceBox: RectF,
        frameWidth: Int,
        frameHeight: Int,
    ): Rect {
        if (frameWidth <= 0 || frameHeight <= 0) return Rect()

        val side = max(faceBox.width(), faceBox.height())
            .roundToInt()
            .coerceAtMost(min(frameWidth, frameHeight))
        if (side <= 0) return Rect()

        val left = (faceBox.centerX() - side / 2f).roundToInt().coerceIn(0, frameWidth - side)
        val top = (faceBox.centerY() - side / 2f).roundToInt().coerceIn(0, frameHeight - side)

        return Rect(left, top, left + side, top + side)
    }

    /**
     * Crops [frame] to [square], scaled down so that neither side exceeds [maxImageSizePx].
     * Returns [frame] untouched if the square does not describe a usable sub-region, so callers
     * always get a bitmap back.
     */
    operator fun invoke(
        frame: Bitmap,
        square: Rect,
        maxImageSizePx: Int,
    ): Bitmap {
        // An in-bounds square that is offset off the edge would make createBitmap throw on the analyzer thread
        if (square.isEmpty || !Rect(0, 0, frame.width, frame.height).contains(square)) {
            return frame
        }
        val crop = Bitmap.createBitmap(frame, square.left, square.top, square.width(), square.height())
        return scaledDownToCap(crop, maxImageSizePx)
    }

    private fun scaledDownToCap(
        crop: Bitmap,
        maxImageSizePx: Int,
    ): Bitmap {
        val longestSide = max(crop.width, crop.height)
        if (longestSide <= maxImageSizePx) return crop

        val scale = maxImageSizePx.toFloat() / longestSide
        val scaled = crop.scale(
            width = (crop.width * scale).roundToInt().coerceAtLeast(1),
            height = (crop.height * scale).roundToInt().coerceAtLeast(1),
        )
        // createScaledBitmap can hand back the source itself when nothing needed doing
        if (scaled !== crop) crop.recycle()
        return scaled
    }
}
