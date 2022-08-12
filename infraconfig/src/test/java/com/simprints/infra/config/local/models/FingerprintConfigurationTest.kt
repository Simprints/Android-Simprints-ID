package com.simprints.infra.config.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.domain.models.FingerprintConfiguration
import com.simprints.infra.config.testtools.fingerprintConfiguration
import com.simprints.infra.config.testtools.protoFingerprintConfiguration
import org.junit.Test

class FingerprintConfigurationTest {

    @Test
    fun `should map correctly the model`() {
        assertThat(protoFingerprintConfiguration.toDomain()).isEqualTo(fingerprintConfiguration)
        assertThat(fingerprintConfiguration.toProto()).isEqualTo(protoFingerprintConfiguration)
    }

    @Test
    fun `should map correctly the Finger enums`() {
        val mapping = mapOf(
            ProtoFingerprintConfiguration.Finger.LEFT_THUMB to FingerprintConfiguration.Finger.LEFT_THUMB,
            ProtoFingerprintConfiguration.Finger.LEFT_INDEX_FINGER to FingerprintConfiguration.Finger.LEFT_INDEX_FINGER,
            ProtoFingerprintConfiguration.Finger.LEFT_3RD_FINGER to FingerprintConfiguration.Finger.LEFT_3RD_FINGER,
            ProtoFingerprintConfiguration.Finger.LEFT_4TH_FINGER to FingerprintConfiguration.Finger.LEFT_4TH_FINGER,
            ProtoFingerprintConfiguration.Finger.LEFT_5TH_FINGER to FingerprintConfiguration.Finger.LEFT_5TH_FINGER,
            ProtoFingerprintConfiguration.Finger.RIGHT_THUMB to FingerprintConfiguration.Finger.RIGHT_THUMB,
            ProtoFingerprintConfiguration.Finger.RIGHT_INDEX_FINGER to FingerprintConfiguration.Finger.RIGHT_INDEX_FINGER,
            ProtoFingerprintConfiguration.Finger.RIGHT_3RD_FINGER to FingerprintConfiguration.Finger.RIGHT_3RD_FINGER,
            ProtoFingerprintConfiguration.Finger.RIGHT_4TH_FINGER to FingerprintConfiguration.Finger.RIGHT_4TH_FINGER,
            ProtoFingerprintConfiguration.Finger.RIGHT_5TH_FINGER to FingerprintConfiguration.Finger.RIGHT_5TH_FINGER,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
            assertThat(it.value.toProto()).isEqualTo(it.key)
        }
    }

    @Test
    fun `should map correctly the VeroGeneration enums`() {
        val mapping = mapOf(
            ProtoFingerprintConfiguration.VeroGeneration.VERO_1 to FingerprintConfiguration.VeroGeneration.VERO_1,
            ProtoFingerprintConfiguration.VeroGeneration.VERO_2 to FingerprintConfiguration.VeroGeneration.VERO_2,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
            assertThat(it.value.toProto()).isEqualTo(it.key)
        }
    }

    @Test
    fun `should map correctly the FingerComparisonStrategy enums`() {
        val mapping = mapOf(
            ProtoFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER to FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
            ProtoFingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX to FingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
            assertThat(it.value.toProto()).isEqualTo(it.key)
        }
    }
}
