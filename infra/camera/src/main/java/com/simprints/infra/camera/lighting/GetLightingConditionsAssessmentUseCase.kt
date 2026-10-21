package com.simprints.infra.camera.lighting

import android.graphics.Bitmap
import com.simprints.infra.camera.lighting.ClassifyLightingConditionsUseCase.Companion.isAbovePercent
import com.simprints.infra.camera.lighting.ClassifyLightingConditionsUseCase.Companion.isBelowPercent
import com.simprints.infra.camera.lighting.LuminanceHistogram.Companion.PERCENTILE_10
import com.simprints.infra.camera.lighting.LuminanceHistogram.Companion.PERCENTILE_90
import com.simprints.infra.camera.lighting.LuminanceHistogram.Companion.PERCENTILE_95
import javax.inject.Inject
import kotlin.math.roundToInt

class GetLightingConditionsAssessmentUseCase @Inject constructor(
    private val classifyLightingConditions: ClassifyLightingConditionsUseCase,
) {
    /**
     * Detects normal, too dim or too bright lighting in a bitmap of an image of a card that approximately fits the image.
     *
     * The basics - abnormal light conditions cause low contrast, that complicates OCR of the image:
     * - if it's so dim that contrast is impacted
     * - if it's so bright that contrast is impacted
     * - if there are light glares at and near which contrast is impacted
     *
     * Default values are used in the definitions below as an example; see LightingConditionsAssessmentConfig
     * Default values are heuristically tested and can be overridden; see ExperimentalProjectConfiguration
     *
     * Definitions:
     * - image: the image in a scaled version of input bitmap, to size of 100 in width or height, whatever is smaller
     * - border width: 5% of image width or height, whatever is smaller
     * - tile: one of the rectangular images that an image in divided into in a grid-like manner, without overlap
     * - luminance: weighted average of red, green, blue values of a pixel, proportionally to human vision sensitivities
     * - P5 percentile: luminance of pixel at 5% of the list of image pixels ordered from lowest luminance to highest
     * - P10 percentile: luminance of pixel at 10% of the list of image pixels ordered from lowest luminance to highest
     * - P50 percentile: luminance of pixel at 50% of the list of image pixels ordered from lowest luminance to highest
     * - P90 percentile: luminance of pixel at 90% of the list of image pixels ordered from lowest luminance to highest
     * - P95 percentile: luminance of pixel at 95% of the list of image pixels ordered from lowest luminance to highest
     * - contrast: difference between luminance of high and low percentiles
     * - brightness: same as luminance; internally luminance is used as a more strict term, externally brightness is okay
     *
     * The image is considered too dim when it's underexposed:
     * - the 5% border width along the edges is ignored to reduce influence of possible bright background behind the card
     * - median P50 luminance is low, <25%
     * - contrast is too low: difference between P95 and P5 luminance <30%
     *
     * Otherwise, the image is considered too bright when it's overexposed:
     * - the 5% border width along the edges is ignored to reduce influence of possible dim background behind the card
     * - median P50 luminance is high, >95%
     * - contrast is too low: difference between P95 and P5 luminance <30%
     *
     * Otherwise, if glare detection is enabled, the image is still considered too bright when it has reflection glare areas:
     * - the 5% border width along the edges is ignored to reduce influence of possible dim background behind the card
     * - the remaining image is divided into a 6x6 (or larger in longer dimension) grid of ~square tiles to look for glare
     * - at least 1 tile has near-maximum P95 luminance very high, >99% (clamped to nearly pure white)
     * - and at the same time low local contrast caused by the glare washout: difference between P90 and P10 luminance <30%
     *
     * Otherwise, the lighting conditions are considered normal.
     */
    operator fun invoke(
        bitmap: Bitmap,
        lightingConditionsAssessmentConfig: LightingConditionsAssessmentConfig,
    ): LightingConditionsAssessment = with(lightingConditionsAssessmentConfig) {
        if (!isEnabled) return LightingConditionsAssessment.NORMAL

        if (bitmap.width <= 0 || bitmap.height <= 0) return LightingConditionsAssessment.NORMAL // nothing to look for
        val scaledBitmap = bitmap.scaledTo(TARGET_MIN_DIMENSION_PX)
        val width = scaledBitmap.width
        val height = scaledBitmap.height

        val pixels = IntArray(width * height)
        scaledBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        if (scaledBitmap !== bitmap) { // can be same if size not changed; we keep the original; see Bitmap.createScaledBitmap
            scaledBitmap.recycle()
        }

        val borderWidth = minOf(width, height) * borderWidthPercent / 100
        val left = borderWidth
        val top = borderWidth
        val right = width - borderWidth
        val bottom = height - borderWidth
        val stride = width

        val imageStats = LuminanceHistogram()
            .apply { addRegion(pixels, stride, left, top, right, bottom) }
            .toLightingStats()
            ?: return LightingConditionsAssessment.NORMAL

        val exposureAssessment = classifyLightingConditions(imageStats, lightingConditionsAssessmentConfig)
        if (exposureAssessment != LightingConditionsAssessment.NORMAL) return exposureAssessment

        if (isGlareDetectionEnabled &&
            hasReflectionGlare(pixels, stride, left, top, right, bottom, lightingConditionsAssessmentConfig)
        ) {
            return LightingConditionsAssessment.TOO_BRIGHT
        }

        return LightingConditionsAssessment.NORMAL
    }

    private fun hasReflectionGlare(
        pixels: IntArray,
        stride: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        lightingConditionsAssessmentConfig: LightingConditionsAssessmentConfig,
    ): Boolean = with(lightingConditionsAssessmentConfig) {
        val innerWidth = right - left
        val innerHeight = bottom - top

        val gridRows = if (innerHeight > innerWidth) {
            glareDetectionGridMinDimension * innerHeight / innerWidth
        } else {
            glareDetectionGridMinDimension
        }
        val gridColumns = if (innerWidth > innerHeight) {
            glareDetectionGridMinDimension * innerWidth / innerHeight
        } else {
            glareDetectionGridMinDimension
        }

        val tileHistogram = LuminanceHistogram()
        for (row in 0 until gridRows) {
            val tileTop = top + row * innerHeight / gridRows
            val tileBottom = top + (row + 1) * innerHeight / gridRows

            for (column in 0 until gridColumns) {
                val tileLeft = left + column * innerWidth / gridColumns
                val tileRight = left + (column + 1) * innerWidth / gridColumns

                tileHistogram.reset()
                tileHistogram.addRegion(pixels, stride, tileLeft, tileTop, tileRight, tileBottom)

                val p10 = tileHistogram.percentile(PERCENTILE_10)
                val p90 = tileHistogram.percentile(PERCENTILE_90)
                val p95 = tileHistogram.percentile(PERCENTILE_95)
                val localContrast = p90 - p10

                if (p95.isAbovePercent(highGlareLuminanceThresholdPercent) &&
                    localContrast.isBelowPercent(lowContrastThresholdPercent)
                ) {
                    return true
                }
            }
        }

        return false
    }

    private fun Bitmap.scaledTo(targetMinDimensionPx: Int): Bitmap {
        val minDimension = minOf(width, height)
        val scaleFactor = targetMinDimensionPx.toFloat() / minDimension.toFloat().coerceAtLeast(1f)
        val scaledWidth = (width * scaleFactor).roundToInt().coerceAtLeast(1)
        val scaledHeight = (height * scaleFactor).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(this, scaledWidth, scaledHeight, true)
    }

    private companion object {
        private const val TARGET_MIN_DIMENSION_PX = 100
    }
}
