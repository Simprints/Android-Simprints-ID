package com.simprints.infra.config.store.models

data class FingerprintConfiguration(
    val allowedScanners: List<VeroGeneration>,
    val allowedSDKs: List<BioSdk>,
    val displayHandIcons: Boolean,
    val secugenSimMatcher: FingerprintSdkConfiguration?,
    val nec: FingerprintSdkConfiguration?,
) {

    data class FingerprintSdkConfiguration(
        val fingersToCapture: List<Finger>,
        val decisionPolicy: DecisionPolicy,
        val comparisonStrategyForVerification: FingerComparisonStrategy,
        val vero1: Vero1Configuration? = null,
        val vero2: Vero2Configuration? = null,
        val allowedAgeRange: AgeGroup? = null,
        val verificationMatchThreshold: Float? = null,
        // TODO [MS-492] agree on format with the Cloud team, and initialize it in all constructor invocations
        val minSuccessfulScansRequired: Int? = null
    )

    enum class VeroGeneration {
        VERO_1,
        VERO_2;
    }

    enum class BioSdk {
        SECUGEN_SIM_MATCHER,
        NEC;
    }

    enum class FingerComparisonStrategy {
        SAME_FINGER,
        CROSS_FINGER_USING_MEAN_OF_MAX;
    }

    fun getSdkConfiguration(sdk: BioSdk): FingerprintSdkConfiguration? = when (sdk) {
        BioSdk.SECUGEN_SIM_MATCHER -> secugenSimMatcher
        BioSdk.NEC -> nec
    }
}
