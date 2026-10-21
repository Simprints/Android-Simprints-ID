package com.simprints.infra.camera.lighting

import kotlin.math.ceil

/**
 * Histogram of pixel luminance values, 0..255.
 * Luminance is a weighted average of red, green and blue, proportional to human vision sensitivities.
 */
internal class LuminanceHistogram {
    private val counts = IntArray(LUMINANCE_BUCKETS)
    var totalCount: Int = 0
        private set

    fun reset() {
        counts.fill(0)
        totalCount = 0
    }

    fun add(color: Int) {
        counts[luminanceOf(color)]++
        totalCount++
    }

    fun addRegion(
        pixels: IntArray,
        stride: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ) {
        for (y in top until bottom) {
            val rowOffset = y * stride
            for (x in left until right) {
                add(pixels[rowOffset + x])
            }
        }
    }

    fun percentile(percentile: Double): Int {
        if (totalCount <= 0) return 0

        val targetRank = ceil(percentile.coerceIn(0.0, 1.0) * totalCount).toInt().coerceAtLeast(1)
        var cumulativeCount = 0

        for (luminance in counts.indices) {
            cumulativeCount += counts[luminance]
            if (cumulativeCount >= targetRank) return luminance
        }

        return counts.lastIndex
    }

    fun toLightingStats(): LightingStats? = if (totalCount <= 0) {
        null
    } else {
        LightingStats(
            p5 = percentile(PERCENTILE_5),
            median = percentile(PERCENTILE_50),
            p95 = percentile(PERCENTILE_95),
        )
    }

    companion object {
        // percentiles
        const val PERCENTILE_5 = 0.05
        const val PERCENTILE_10 = 0.10
        const val PERCENTILE_50 = 0.50
        const val PERCENTILE_90 = 0.90
        const val PERCENTILE_95 = 0.95

        // color math
        private const val LUMINANCE_BUCKETS = 256
        const val MAX_LUMINANCE = LUMINANCE_BUCKETS - 1
        private const val CHANNEL_MASK = 0xFF
        private const val RED_WEIGHT = 77
        private const val GREEN_WEIGHT = 150
        private const val BLUE_WEIGHT = 29

        private fun luminanceOf(color: Int): Int {
            val red = (color ushr 16) and CHANNEL_MASK
            val green = (color ushr 8) and CHANNEL_MASK
            val blue = color and CHANNEL_MASK
            return (RED_WEIGHT * red + GREEN_WEIGHT * green + BLUE_WEIGHT * blue) ushr 8 // int approximation
        }
    }
}
