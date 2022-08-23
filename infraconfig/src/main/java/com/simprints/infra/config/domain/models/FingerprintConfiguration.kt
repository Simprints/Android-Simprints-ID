package com.simprints.infra.config.domain.models

data class FingerprintConfiguration(
    val fingersToCapture: List<Finger>,
    val qualityThreshold: Int,
    val decisionPolicy: DecisionPolicy,
    val allowedVeroGenerations: List<VeroGeneration>,
    val comparisonStrategyForVerification: FingerComparisonStrategy,
    val displayHandIcons: Boolean,
    val vero2: Vero2Configuration?
) {

    enum class VeroGeneration {
        VERO_1,
        VERO_2;
    }

    enum class FingerComparisonStrategy {
        SAME_FINGER,
        CROSS_FINGER_USING_MEAN_OF_MAX;
    }
}
