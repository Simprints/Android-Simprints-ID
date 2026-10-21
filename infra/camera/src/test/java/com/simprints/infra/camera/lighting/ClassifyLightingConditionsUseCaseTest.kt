package com.simprints.infra.camera.lighting

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class ClassifyLightingConditionsUseCaseTest {
    private val useCase = ClassifyLightingConditionsUseCase()

    @Test
    fun `too dim when median and contrast are low`() {
        assertThat(useCase(stats(median = 30, contrast = 40), config())).isEqualTo(LightingConditionsAssessment.TOO_DIM)
    }

    @Test
    fun `normal when dark but contrast is high`() {
        assertThat(useCase(stats(median = 30, contrast = 120), config())).isEqualTo(LightingConditionsAssessment.NORMAL)
    }

    @Test
    fun `contrast threshold of 100 percent checks median only`() {
        val config = config(lowContrastThresholdPercent = 100, lowMedianLuminanceThresholdPercent = 14)

        assertThat(useCase(stats(median = 34, contrast = 200), config)).isEqualTo(LightingConditionsAssessment.TOO_DIM)
        assertThat(useCase(stats(median = 36, contrast = 10), config)).isEqualTo(LightingConditionsAssessment.NORMAL)
    }

    @Test
    fun `too bright when median is high and contrast is low`() {
        assertThat(useCase(stats(median = 250, contrast = 5), config())).isEqualTo(LightingConditionsAssessment.TOO_BRIGHT)
    }

    @Test
    fun `high brightness threshold of 100 percent never reports too bright`() {
        val config = config(highMedianLuminanceThresholdPercent = 100)

        assertThat(useCase(stats(median = 255, contrast = 0), config)).isEqualTo(LightingConditionsAssessment.NORMAL)
    }

    @Test
    fun `normal when disabled`() {
        assertThat(useCase(stats(median = 0, contrast = 0), config(isEnabled = false))).isEqualTo(LightingConditionsAssessment.NORMAL)
    }

    private fun stats(
        median: Int,
        contrast: Int,
    ) = LightingStats(p5 = 0, median = median, p95 = contrast)

    private fun config(
        isEnabled: Boolean = true,
        lowContrastThresholdPercent: Int = 30,
        lowMedianLuminanceThresholdPercent: Int = 25,
        highMedianLuminanceThresholdPercent: Int = 95,
    ) = LightingConditionsAssessmentConfig(
        isEnabled = isEnabled,
        borderWidthPercent = 5,
        lowContrastThresholdPercent = lowContrastThresholdPercent,
        lowMedianLuminanceThresholdPercent = lowMedianLuminanceThresholdPercent,
        highMedianLuminanceThresholdPercent = highMedianLuminanceThresholdPercent,
        highGlareLuminanceThresholdPercent = 99,
        glareDetectionGridMinDimension = 6,
    )
}
