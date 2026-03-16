package com.simprints.feature.externalcredential.screens.scanocr.model

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports

@ExcludedFromGeneratedTestCoverageReports("Data struct")
internal data class LightingConditionsAssessmentConfig(
    val isEnabled: Boolean,
    val borderWidthPercent: Int,
    val lowContrastThresholdPercent: Int,
    val lowMedianLuminanceThresholdPercent: Int,
    val highMedianLuminanceThresholdPercent: Int,
    val highGlareLuminanceThresholdPercent: Int,
    val glareDetectionGridMinDimension: Int,
)
