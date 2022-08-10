package com.simprints.infra.config.domain

data class FingerprintConfiguration(
    val fingersToCapture: List<Finger>,
    val qualityThreshold: Int,
    val decisionPolicy: DecisionPolicy,
    val allowedVeroGenerations: List<VeroGeneration>,
    val comparisonStrategyForVerification: FingerComparisonStrategy,
    val displayHandIcons: Boolean,
    val vero2: Vero2Configuration?
) {

    enum class Finger {
        LEFT_THUMB,
        LEFT_INDEX_FINGER,
        LEFT_3RD_FINGER,
        LEFT_4TH_FINGER,
        LEFT_5TH_FINGER,
        RIGHT_THUMB,
        RIGHT_INDEX_FINGER,
        RIGHT_3RD_FINGER,
        RIGHT_4TH_FINGER,
        RIGHT_5TH_FINGER;
    }

    enum class VeroGeneration {
        VERO_1,
        VERO_2;
    }

    enum class FingerComparisonStrategy {
        SAME_FINGER,
        CROSS_FINGER_USING_MEAN_OF_MAX;
    }
}
