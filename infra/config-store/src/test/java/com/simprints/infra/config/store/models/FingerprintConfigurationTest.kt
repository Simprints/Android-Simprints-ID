package com.simprints.infra.config.store.models

import com.google.common.truth.Truth
import org.junit.Test

class FingerprintConfigurationTest {

    @Test
    fun `should retrieve secugenSimMatcher from bioSdkConfiguration if secugenSimMatcher not null  `() {

        val fingerprintConfiguration = FingerprintConfiguration(
            allowedScanners = listOf(FingerprintConfiguration.VeroGeneration.VERO_1),
            allowedSDKs = listOf(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER),
            displayHandIcons = true,
            secugenSimMatcher = FingerprintConfiguration.FingerprintSdkConfiguration(
                fingersToCapture = listOf(Finger.LEFT_THUMB),
                decisionPolicy = DecisionPolicy(20, 50, 100),
                comparisonStrategyForVerification = FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
                vero1 = Vero1Configuration(60),
                vero2 = null,
            ),
            nec = null,
        )
        Truth.assertThat(fingerprintConfiguration.bioSdkConfiguration)
            .isEqualTo(fingerprintConfiguration.secugenSimMatcher)
    }

    @Test
    fun `should retrieve nec from bioSdkConfiguration if nec not null  `() {

        val fingerprintConfiguration = FingerprintConfiguration(
            allowedScanners = listOf(FingerprintConfiguration.VeroGeneration.VERO_1),
            allowedSDKs = listOf(FingerprintConfiguration.BioSdk.NEC),
            displayHandIcons = true,
            secugenSimMatcher = null,
            nec = FingerprintConfiguration.FingerprintSdkConfiguration(
                fingersToCapture = listOf(Finger.LEFT_THUMB),
                decisionPolicy = DecisionPolicy(20, 50, 100),
                comparisonStrategyForVerification = FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
                vero1 = Vero1Configuration(60),
                vero2 = null,
            ),
        )
        Truth.assertThat(fingerprintConfiguration.bioSdkConfiguration)
            .isEqualTo(fingerprintConfiguration.nec)
    }

    @Test(expected = IllegalStateException::class)
    fun `should throw IllegalStateException if nec and secugenSimMatcher are  null  `() {

        val fingerprintConfiguration = FingerprintConfiguration(
            allowedScanners = listOf(FingerprintConfiguration.VeroGeneration.VERO_1),
            allowedSDKs = listOf(FingerprintConfiguration.BioSdk.NEC),
            displayHandIcons = true,
            secugenSimMatcher = null,
            nec = null,
        )
        fingerprintConfiguration.bioSdkConfiguration
    }


}
