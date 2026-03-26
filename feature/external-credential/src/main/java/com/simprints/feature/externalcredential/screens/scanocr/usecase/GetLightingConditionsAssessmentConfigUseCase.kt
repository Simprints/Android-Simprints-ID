package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.simprints.feature.externalcredential.screens.scanocr.model.LightingConditionsAssessmentConfig
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration
import com.simprints.infra.config.store.models.experimental
import javax.inject.Inject

internal class GetLightingConditionsAssessmentConfigUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
) {
    suspend operator fun invoke(): LightingConditionsAssessmentConfig? = configRepository
        .getProjectConfiguration()
        .experimental()
        .toLightingConditionsAssessmentConfig()
        .takeIf(LightingConditionsAssessmentConfig::isEnabled)

    private fun ExperimentalProjectConfiguration.toLightingConditionsAssessmentConfig() = LightingConditionsAssessmentConfig(
        isEnabled = mfidLightingConditionsAssessmentEnabled,
        borderWidthPercent = mfidLightingConditionsAssessmentPadding,
        lowContrastThresholdPercent = mfidLightingConditionsAssessmentLowContrast,
        lowMedianLuminanceThresholdPercent = mfidLightingConditionsAssessmentLowBrightness,
        highMedianLuminanceThresholdPercent = mfidLightingConditionsAssessmentHighBrightness,
        highGlareLuminanceThresholdPercent = mfidLightingConditionsAssessmentGlareBrightness,
        glareDetectionGridMinDimension = mfidLightingConditionsAssessmentGlareSensitivity,
    )
}
