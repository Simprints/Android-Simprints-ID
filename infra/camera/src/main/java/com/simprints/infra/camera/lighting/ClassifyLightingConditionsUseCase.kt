package com.simprints.infra.camera.lighting

import com.simprints.infra.camera.lighting.LuminanceHistogram.Companion.MAX_LUMINANCE
import javax.inject.Inject

/**
 * Classifies overall exposure from [LightingStats]; does not look for local glare.
 *
 * - too dim (underexposed): median luminance below the low threshold and contrast below the contrast threshold
 * - too bright (overexposed): median luminance above the high threshold and contrast below the contrast threshold
 * - otherwise normal
 *
 * A contrast threshold of 100% or more turns the contrast condition off, so only the median is checked.
 */
class ClassifyLightingConditionsUseCase @Inject constructor() {
    operator fun invoke(
        stats: LightingStats,
        config: LightingConditionsAssessmentConfig,
    ): LightingConditionsAssessment = with(config) {
        if (!isEnabled) return LightingConditionsAssessment.NORMAL

        if (stats.median.isBelowPercent(lowMedianLuminanceThresholdPercent) &&
            isLowContrast(stats.contrast)
        ) {
            return LightingConditionsAssessment.TOO_DIM
        }

        if (stats.median.isAbovePercent(highMedianLuminanceThresholdPercent) &&
            isLowContrast(stats.contrast)
        ) {
            return LightingConditionsAssessment.TOO_BRIGHT
        }

        return LightingConditionsAssessment.NORMAL
    }

    private fun LightingConditionsAssessmentConfig.isLowContrast(contrast: Int): Boolean =
        lowContrastThresholdPercent >= 100 || contrast.isBelowPercent(lowContrastThresholdPercent)

    internal companion object {
        fun Int.isBelowPercent(percent: Int): Boolean = this * 100 < percent * MAX_LUMINANCE

        fun Int.isAbovePercent(percent: Int): Boolean = this * 100 > percent * MAX_LUMINANCE
    }
}
