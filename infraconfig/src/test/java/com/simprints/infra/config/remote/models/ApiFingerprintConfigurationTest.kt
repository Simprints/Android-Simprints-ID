package com.simprints.infra.config.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.domain.models.Finger
import com.simprints.infra.config.domain.models.FingerprintConfiguration
import com.simprints.infra.config.testtools.*
import com.simprints.infra.config.testtools.apiDecisionPolicy
import com.simprints.infra.config.testtools.apiFingerprintConfiguration
import com.simprints.infra.config.testtools.decisionPolicy
import com.simprints.infra.config.testtools.fingerprintConfiguration
import com.simprints.infra.config.testtools.vero2Configuration
import org.junit.Test

class ApiFingerprintConfigurationTest {

    @Test
    fun `should map correctly the model`() {
        assertThat(apiFingerprintConfiguration.toDomain()).isEqualTo(fingerprintConfiguration)
    }

    @Test
    fun `should map correctly the model when the vero2 is missing`() {
        val apiFingerprintConfiguration = ApiFingerprintConfiguration(
            listOf(ApiFingerprintConfiguration.Finger.LEFT_3RD_FINGER),
            10,
            apiDecisionPolicy,
            listOf(ApiFingerprintConfiguration.VeroGeneration.VERO_2),
            ApiFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
            true,
            null,
        )
        val fingerprintConfiguration = FingerprintConfiguration(
            listOf(Finger.LEFT_3RD_FINGER),
            10,
            decisionPolicy,
            listOf(FingerprintConfiguration.VeroGeneration.VERO_2),
            FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
            true,
            null,
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
