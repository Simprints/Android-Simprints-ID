package com.simprints.infra.camera.lighting

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports

@ExcludedFromGeneratedTestCoverageReports("Data struct")
data class LightingConditionsAssessmentConfig(
    val isEnabled: Boolean,
    val borderWidthPercent: Int,
    val lowContrastThresholdPercent: Int,
    val lowMedianLuminanceThresholdPercent: Int,
    val highMedianLuminanceThresholdPercent: Int,
    val highGlareLuminanceThresholdPercent: Int,
    val glareDetectionGridMinDimension: Int,
    /** Glare detection suits flat reflective surfaces like cards; it gives false alarms on faces. */
    val isGlareDetectionEnabled: Boolean = true,
)
