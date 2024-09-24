package com.simprints.infra.config.store.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.Finger
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.MaxCaptureAttempts
import com.simprints.infra.config.store.models.Vero1Configuration
import com.simprints.infra.config.store.testtools.apiDecisionPolicy
import com.simprints.infra.config.store.testtools.apiFingerprintConfiguration
import com.simprints.infra.config.store.testtools.apiMaxCaptureAttempts
import com.simprints.infra.config.store.testtools.decisionPolicy
import com.simprints.infra.config.store.testtools.fingerprintConfiguration
import org.junit.Test

class ApiFingerprintConfigurationTest {

    @Test
    fun `should map correctly the model`() {
        assertThat(apiFingerprintConfiguration.toDomain()).isEqualTo(fingerprintConfiguration)
    }

    @Test
    fun `should map correctly the model with allowedAgeRange null`() {
        val apiFingerprintConfigurationWithAgeRange = apiFingerprintConfiguration.copy(
            secugenSimMatcher = apiFingerprintConfiguration.secugenSimMatcher?.copy(
                allowedAgeRange = null
            )
        )
        val fingerprintConfigurationWithAgeRange = fingerprintConfiguration.copy(
            secugenSimMatcher = fingerprintConfiguration.secugenSimMatcher?.copy(
                allowedAgeRange = AgeGroup(0, null)
            )
        )
        assertThat(apiFingerprintConfigurationWithAgeRange.toDomain()).isEqualTo(fingerprintConfigurationWithAgeRange)
    }

    @Test
    fun `should map correctly the model when the vero1 is missing`() {
        val apiFingerprintConfiguration = ApiFingerprintConfiguration(
            listOf(ApiFingerprintConfiguration.VeroGeneration.VERO_2),
            listOf(ApiFingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER),
            true,
            ApiFingerprintConfiguration.ApiFingerprintSdkConfiguration(
                fingersToCapture = listOf(ApiFingerprintConfiguration.Finger.LEFT_3RD_FINGER),
                decisionPolicy = apiDecisionPolicy,
                comparisonStrategyForVerification = ApiFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
                vero1 = null,
                vero2 = apiFingerprintConfiguration.secugenSimMatcher?.vero2,
                allowedAgeRange = apiFingerprintConfiguration.secugenSimMatcher?.allowedAgeRange!!,
                maxCaptureAttempts = apiMaxCaptureAttempts
            ),
            null,
        )
        val fingerprintConfiguration = FingerprintConfiguration(
            listOf(FingerprintConfiguration.VeroGeneration.VERO_2),
            listOf(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER),
            true,
            FingerprintConfiguration.FingerprintSdkConfiguration(
                fingersToCapture = listOf(Finger.LEFT_3RD_FINGER),
                decisionPolicy = decisionPolicy,
                comparisonStrategyForVerification = FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
                vero1 = null,
                vero2 = fingerprintConfiguration.secugenSimMatcher?.vero2,
                allowedAgeRange = fingerprintConfiguration.secugenSimMatcher?.allowedAgeRange!!,
                verificationMatchThreshold = null,
                maxCaptureAttempts = MaxCaptureAttempts(noFingerDetected = 17)
            ),
            null,
        )

        assertThat(apiFingerprintConfiguration.toDomain()).isEqualTo(fingerprintConfiguration)
    }

    @Test
    fun `should map correctly the model when the vero2 is missing`() {
        val apiFingerprintConfiguration = ApiFingerprintConfiguration(
            listOf(ApiFingerprintConfiguration.VeroGeneration.VERO_1),
            listOf(ApiFingerprintConfiguration.BioSdk.NEC),
            true,
            null,
            ApiFingerprintConfiguration.ApiFingerprintSdkConfiguration(
                fingersToCapture = listOf(ApiFingerprintConfiguration.Finger.LEFT_3RD_FINGER),
                decisionPolicy = apiDecisionPolicy,
                comparisonStrategyForVerification = ApiFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
                vero1 = ApiVero1Configuration(10),
                vero2 = null,
                allowedAgeRange = apiFingerprintConfiguration.secugenSimMatcher?.allowedAgeRange!!,
                maxCaptureAttempts = apiMaxCaptureAttempts
            ),

            )
        val fingerprintConfiguration = FingerprintConfiguration(
            listOf(FingerprintConfiguration.VeroGeneration.VERO_1),
            listOf(FingerprintConfiguration.BioSdk.NEC),
            true,
            null,
            FingerprintConfiguration.FingerprintSdkConfiguration(
                fingersToCapture = listOf(Finger.LEFT_3RD_FINGER),
                decisionPolicy = decisionPolicy,
                comparisonStrategyForVerification = FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
                vero1 = Vero1Configuration(10),
                vero2 = null,
                allowedAgeRange = fingerprintConfiguration.secugenSimMatcher?.allowedAgeRange!!,
                verificationMatchThreshold = null,
                maxCaptureAttempts = MaxCaptureAttempts(noFingerDetected = 17)
            ),

            )

        assertThat(apiFingerprintConfiguration.toDomain()).isEqualTo(fingerprintConfiguration)
    }

    @Test
    fun `should map correctly the Finger enums`() {
        val mapping = mapOf(
            ApiFingerprintConfiguration.Finger.LEFT_THUMB to Finger.LEFT_THUMB,
            ApiFingerprintConfiguration.Finger.LEFT_INDEX_FINGER to Finger.LEFT_INDEX_FINGER,
            ApiFingerprintConfiguration.Finger.LEFT_3RD_FINGER to Finger.LEFT_3RD_FINGER,
            ApiFingerprintConfiguration.Finger.LEFT_4TH_FINGER to Finger.LEFT_4TH_FINGER,
            ApiFingerprintConfiguration.Finger.LEFT_5TH_FINGER to Finger.LEFT_5TH_FINGER,
            ApiFingerprintConfiguration.Finger.RIGHT_THUMB to Finger.RIGHT_THUMB,
            ApiFingerprintConfiguration.Finger.RIGHT_INDEX_FINGER to Finger.RIGHT_INDEX_FINGER,
            ApiFingerprintConfiguration.Finger.RIGHT_3RD_FINGER to Finger.RIGHT_3RD_FINGER,
            ApiFingerprintConfiguration.Finger.RIGHT_4TH_FINGER to Finger.RIGHT_4TH_FINGER,
            ApiFingerprintConfiguration.Finger.RIGHT_5TH_FINGER to Finger.RIGHT_5TH_FINGER,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
        }
    }

    @Test
    fun `should map correctly the VeroGeneration enums`() {
        val mapping = mapOf(
            ApiFingerprintConfiguration.VeroGeneration.VERO_1 to FingerprintConfiguration.VeroGeneration.VERO_1,
            ApiFingerprintConfiguration.VeroGeneration.VERO_2 to FingerprintConfiguration.VeroGeneration.VERO_2,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
        }
    }

    @Test
    fun `should map correctly the FingerComparisonStrategy enums`() {
        val mapping = mapOf(
            ApiFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER to FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
            ApiFingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX to FingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
        }
    }
}
