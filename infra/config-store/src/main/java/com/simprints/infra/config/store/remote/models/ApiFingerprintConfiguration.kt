package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.Finger as DomainFingerprint

@Keep
internal data class ApiFingerprintConfiguration(
    val allowedScanners: List<VeroGeneration>,
    val allowedSDKs: List<BioSdk>,
    val displayHandIcons: Boolean,
    val secugenSimMatcher: ApiFingerprintSdkConfiguration?,
    val nec: ApiFingerprintSdkConfiguration?,
) {

    fun toDomain() = FingerprintConfiguration(
        allowedScanners.map { it.toDomain() },
        allowedSDKs.map { it.toDomain() },
        displayHandIcons,
        secugenSimMatcher?.toDomain(),
        nec?.toDomain(),
    )

    @Keep
    data class ApiFingerprintSdkConfiguration(
        val fingersToCapture: List<Finger>,
        val decisionPolicy: ApiDecisionPolicy,
        val comparisonStrategyForVerification: FingerComparisonStrategy,
        val vero1: ApiVero1Configuration? = null,
        val vero2: ApiVero2Configuration? = null,
        val allowedAgeRange: ApiAllowedAgeRange? = null,
        val verificationMatchThreshold: Float? = null,
        val maxCaptureAttempts: ApiMaxCaptureAttempts? = null
    ) {
        fun toDomain() = FingerprintConfiguration.FingerprintSdkConfiguration(
            fingersToCapture = fingersToCapture.map { it.toDomain() },
            decisionPolicy = decisionPolicy.toDomain(),
            comparisonStrategyForVerification = comparisonStrategyForVerification.toDomain(),
            vero1 = vero1?.toDomain(),
            vero2 = vero2?.toDomain(),
            allowedAgeRange = allowedAgeRange?.toDomain() ?: AgeGroup(0, null),
            verificationMatchThreshold = verificationMatchThreshold,
            maxCaptureAttempts = maxCaptureAttempts?.toDomain()
        )
    }

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

        fun toDomain() = when (this) {
            LEFT_THUMB -> DomainFingerprint.LEFT_THUMB
            LEFT_INDEX_FINGER -> DomainFingerprint.LEFT_INDEX_FINGER
            LEFT_3RD_FINGER -> DomainFingerprint.LEFT_3RD_FINGER
            LEFT_4TH_FINGER -> DomainFingerprint.LEFT_4TH_FINGER
            LEFT_5TH_FINGER -> DomainFingerprint.LEFT_5TH_FINGER
            RIGHT_THUMB -> DomainFingerprint.RIGHT_THUMB
            RIGHT_INDEX_FINGER -> DomainFingerprint.RIGHT_INDEX_FINGER
            RIGHT_3RD_FINGER -> DomainFingerprint.RIGHT_3RD_FINGER
            RIGHT_4TH_FINGER -> DomainFingerprint.RIGHT_4TH_FINGER
            RIGHT_5TH_FINGER -> DomainFingerprint.RIGHT_5TH_FINGER
        }
    }

    @Keep
    enum class VeroGeneration {
        VERO_1,
        VERO_2;

        fun toDomain() = when (this) {
            VERO_1 -> FingerprintConfiguration.VeroGeneration.VERO_1
            VERO_2 -> FingerprintConfiguration.VeroGeneration.VERO_2
        }
    }

    @Keep
    enum class BioSdk {
        SECUGEN_SIM_MATCHER,
        NEC;

        fun toDomain() = when (this) {
            SECUGEN_SIM_MATCHER -> FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
            NEC -> FingerprintConfiguration.BioSdk.NEC
        }
    }

    @Keep
    enum class FingerComparisonStrategy {
        SAME_FINGER,
        CROSS_FINGER_USING_MEAN_OF_MAX;

        fun toDomain() = when (this) {
            SAME_FINGER -> FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER
            CROSS_FINGER_USING_MEAN_OF_MAX -> FingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX
        }
    }
}
