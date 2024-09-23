package com.simprints.infra.config.store.models

import com.google.common.truth.Truth
import org.junit.Test

class FingerprintConfigurationTest {

    @Test
    fun `should retrieve SecugenSimMatcher's configuration  when SECUGEN_SIM_MATCHER is requested `() {

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
                maxCaptureAttempts = MaxCaptureAttempts(noFingerDetected = 17)
            ),
            nec = null,
        )
        Truth.assertThat(fingerprintConfiguration.getSdkConfiguration(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER))
            .isEqualTo(fingerprintConfiguration.secugenSimMatcher)
    }

    @Test
    fun `should retrieve NEC's configuration  when NEC is requested `() {

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
                maxCaptureAttempts = MaxCaptureAttempts(noFingerDetected = 17)
            ),
        )
        Truth.assertThat(fingerprintConfiguration.getSdkConfiguration(FingerprintConfiguration.BioSdk.NEC))
            .isEqualTo(fingerprintConfiguration.nec)
    }
}
