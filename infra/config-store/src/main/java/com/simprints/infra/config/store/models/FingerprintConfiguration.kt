package com.simprints.infra.config.store.models

import com.simprints.core.domain.common.AgeGroup
import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.infra.config.store.models.ModalitySdkType

data class FingerprintConfiguration(
    val allowedScanners: List<VeroGeneration>,
    val allowedSDKs: List<ModalitySdkType>,
    val displayHandIcons: Boolean,
    val secugenSimMatcher: FingerprintSdkConfiguration?,
    val nec: FingerprintSdkConfiguration?,
) {
    data class FingerprintSdkConfiguration(
        val fingersToCapture: List<TemplateIdentifier>,
        override val decisionPolicy: DecisionPolicy,
        val comparisonStrategyForVerification: FingerComparisonStrategy,
        val vero1: Vero1Configuration? = null,
        val vero2: Vero2Configuration? = null,
        override val allowedAgeRange: AgeGroup = AgeGroup(0, null),
        override val verificationMatchThreshold: Float? = null,
        /**
         * Allowed amount of 'No Finger Detected' scans before proceeding further
         */
        val maxCaptureAttempts: MaxCaptureAttempts?,
        val version: String = "",
    ) : ModalitySdkConfiguration

    enum class VeroGeneration {
        VERO_1,
        VERO_2,
    }

    enum class FingerComparisonStrategy {
        SAME_FINGER,
        CROSS_FINGER_USING_MEAN_OF_MAX,
    }

    fun getSdkConfiguration(sdk: ModalitySdkType): FingerprintSdkConfiguration? = when (sdk) {
        ModalitySdkType.SECUGEN_SIM_MATCHER -> secugenSimMatcher
        ModalitySdkType.NEC -> nec
        else -> null
    }
}
