package com.simprints.infra.config.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.domain.FingerprintConfiguration

@Keep
internal data class ApiFingerprintConfiguration(
    val fingersToCapture: List<Finger>,
    val qualityThreshold: Int,
    val decisionPolicy: ApiDecisionPolicy,
    val allowedVeroGenerations: List<VeroGeneration>,
    val comparisonStrategyForVerification: FingerComparisonStrategy,
    val displayHandIcons: Boolean,
    val vero2: ApiVero2Configuration?
) {

    fun toDomain(): FingerprintConfiguration =
        FingerprintConfiguration(
            fingersToCapture.map { it.toDomain() },
            qualityThreshold,
            decisionPolicy.toDomain(),
            allowedVeroGenerations.map { it.toDomain() },
            comparisonStrategyForVerification.toDomain(),
            displayHandIcons,
            vero2?.toDomain()
        )

    @Keep
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

        fun toDomain(): FingerprintConfiguration.Finger =
            when (this) {
                LEFT_THUMB -> FingerprintConfiguration.Finger.LEFT_THUMB
                LEFT_INDEX_FINGER -> FingerprintConfiguration.Finger.LEFT_INDEX_FINGER
                LEFT_3RD_FINGER -> FingerprintConfiguration.Finger.LEFT_3RD_FINGER
                LEFT_4TH_FINGER -> FingerprintConfiguration.Finger.LEFT_4TH_FINGER
                LEFT_5TH_FINGER -> FingerprintConfiguration.Finger.LEFT_5TH_FINGER
                RIGHT_THUMB -> FingerprintConfiguration.Finger.RIGHT_THUMB
                RIGHT_INDEX_FINGER -> FingerprintConfiguration.Finger.RIGHT_INDEX_FINGER
                RIGHT_3RD_FINGER -> FingerprintConfiguration.Finger.RIGHT_3RD_FINGER
                RIGHT_4TH_FINGER -> FingerprintConfiguration.Finger.RIGHT_4TH_FINGER
                RIGHT_5TH_FINGER -> FingerprintConfiguration.Finger.RIGHT_5TH_FINGER
            }
    }

    @Keep
    enum class VeroGeneration {
        VERO_1,
        VERO_2;

        fun toDomain(): FingerprintConfiguration.VeroGeneration =
            when (this) {
                VERO_1 -> FingerprintConfiguration.VeroGeneration.VERO_1
                VERO_2 -> FingerprintConfiguration.VeroGeneration.VERO_2
            }
    }

    @Keep
    enum class FingerComparisonStrategy {
        SAME_FINGER,
        CROSS_FINGER_USING_MEAN_OF_MAX;

        fun toDomain(): FingerprintConfiguration.FingerComparisonStrategy =
            when (this) {
                SAME_FINGER -> FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER
                CROSS_FINGER_USING_MEAN_OF_MAX -> FingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX
            }
    }
}
