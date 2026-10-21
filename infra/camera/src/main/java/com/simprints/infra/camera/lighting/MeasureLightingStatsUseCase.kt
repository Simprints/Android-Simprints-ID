package com.simprints.infra.camera.lighting

import android.graphics.Bitmap
import android.graphics.RectF
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Measures [LightingStats] of a region of a bitmap, sampling about [TARGET_MIN_DIMENSION_PX] pixels
 * along the shorter side of the region.
 *
 * Meant to run on every few camera frames, so it reuses its buffers between calls and allocates no bitmaps.
 * Not thread-safe: use one instance per frame-processing pipeline.
 */
class MeasureLightingStatsUseCase @Inject constructor() {
    private val histogram = LuminanceHistogram()
    private var rowBuffer = IntArray(0)

    /**
     * @param region relative (0..1) coordinates of the region to measure, or null for the whole bitmap
     * @param borderWidthPercent part of the shorter side of the region to ignore along its edges
     * @return null when there is nothing to measure
     */
    operator fun invoke(
        bitmap: Bitmap,
        region: RectF? = null,
        borderWidthPercent: Int = 0,
    ): LightingStats? {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return null

        var left = 0
        var top = 0
        var right = bitmap.width
        var bottom = bitmap.height
        if (region != null) {
            left = (region.left * bitmap.width).roundToInt().coerceIn(0, bitmap.width)
            top = (region.top * bitmap.height).roundToInt().coerceIn(0, bitmap.height)
            right = (region.right * bitmap.width).roundToInt().coerceIn(left, bitmap.width)
            bottom = (region.bottom * bitmap.height).roundToInt().coerceIn(top, bitmap.height)
        }

        val minDimension = minOf(right - left, bottom - top)
        if (minDimension <= 0) return null
        val borderWidth = minDimension * borderWidthPercent.coerceIn(0, MAX_BORDER_WIDTH_PERCENT) / 100
        left += borderWidth
        top += borderWidth
        right -= borderWidth
        bottom -= borderWidth
        val rowWidth = right - left
        if (rowWidth <= 0 || bottom <= top) return null

        // Nearest-neighbour sampling is enough for percentiles and avoids scaling the bitmap
        val step = (minDimension / TARGET_MIN_DIMENSION_PX).coerceAtLeast(1)
        if (rowBuffer.size < rowWidth) rowBuffer = IntArray(rowWidth)

        histogram.reset()
        for (y in top until bottom step step) {
            bitmap.getPixels(rowBuffer, 0, rowWidth, left, y, rowWidth, 1)
            for (x in 0 until rowWidth step step) {
                histogram.add(rowBuffer[x])
            }
        }
        return histogram.toLightingStats()
    }

    private companion object {
        private const val TARGET_MIN_DIMENSION_PX = 100
        private const val MAX_BORDER_WIDTH_PERCENT = 49
    }
}
