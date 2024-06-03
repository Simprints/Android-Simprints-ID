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
        val allowedAgeRange: IntRange? = null,
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

    // Todo we didn't yet implement the logic to select the SDK based on the configuration
    // so we are just using  secugenSimMatcher if it is not null or nec otherwise
    // See ticket SIM-81 for more details
    val bioSdkConfiguration: FingerprintSdkConfiguration
        get() = when {
            secugenSimMatcher != null -> secugenSimMatcher
            nec != null -> nec
            else -> throw IllegalStateException("No active BioSdk")
        }

}
