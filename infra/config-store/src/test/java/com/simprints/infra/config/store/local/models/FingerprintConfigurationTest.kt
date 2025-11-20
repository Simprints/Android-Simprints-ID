package com.simprints.infra.config.store.local.models

import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.AgeGroup
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.testtools.fingerprintConfiguration
import com.simprints.infra.config.store.testtools.protoFingerprintConfiguration
import org.junit.Test

class FingerprintConfigurationTest {
    @Test
    fun `should map correctly the model`() {
        assertThat(protoFingerprintConfiguration.toDomain()).isEqualTo(fingerprintConfiguration)
        assertThat(fingerprintConfiguration.toProto()).isEqualTo(protoFingerprintConfiguration)
    }

    @Test
    fun `should map correctly the model with allowedAgeRange missing`() {
        val protoFingerprintConfigurationWithoutAgeRange = protoFingerprintConfiguration
            .toBuilder()
            .setSecugenSimMatcher(protoFingerprintConfiguration.secugenSimMatcher.toBuilder().clearAllowedAgeRange())
            .build()
        val fingerprintConfigurationWithoutAgeRange = fingerprintConfiguration.copy(
            secugenSimMatcher = fingerprintConfiguration.secugenSimMatcher?.copy(
                allowedAgeRange = AgeGroup(0, null),
            ),
        )

        assertThat(protoFingerprintConfigurationWithoutAgeRange.toDomain()).isEqualTo(fingerprintConfigurationWithoutAgeRange)
    }

    @Test
    fun `should map correctly the old model to new model`() {
        val proto = protoFingerprintConfiguration
            .toBuilder()
            .clearNec()
            .clearSecugenSimMatcher()
            .addAllowedVeroGenerations(ProtoFingerprintConfiguration.VeroGeneration.VERO_2)
            .build()
        assertThat(proto.toDomain().allowedScanners).contains(FingerprintConfiguration.VeroGeneration.VERO_2)
    }

    @Test
    fun `should map correctly the Finger enums`() {
        val mapping = mapOf(
            ProtoFinger.LEFT_THUMB to SampleIdentifier.LEFT_THUMB,
            ProtoFinger.LEFT_INDEX_FINGER to SampleIdentifier.LEFT_INDEX_FINGER,
            ProtoFinger.LEFT_3RD_FINGER to SampleIdentifier.LEFT_3RD_FINGER,
            ProtoFinger.LEFT_4TH_FINGER to SampleIdentifier.LEFT_4TH_FINGER,
            ProtoFinger.LEFT_5TH_FINGER to SampleIdentifier.LEFT_5TH_FINGER,
            ProtoFinger.RIGHT_THUMB to SampleIdentifier.RIGHT_THUMB,
            ProtoFinger.RIGHT_INDEX_FINGER to SampleIdentifier.RIGHT_INDEX_FINGER,
            ProtoFinger.RIGHT_3RD_FINGER to SampleIdentifier.RIGHT_3RD_FINGER,
            ProtoFinger.RIGHT_4TH_FINGER to SampleIdentifier.RIGHT_4TH_FINGER,
            ProtoFinger.RIGHT_5TH_FINGER to SampleIdentifier.RIGHT_5TH_FINGER,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
            assertThat(it.value.toProtoFinger()).isEqualTo(it.key)
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
            ProtoFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER to
                FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
            ProtoFingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX to
                FingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
            assertThat(it.value.toProto()).isEqualTo(it.key)
        }
    }
}
