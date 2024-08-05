package com.simprints.infra.config.store.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.testtools.faceConfiguration
import com.simprints.infra.config.store.testtools.protoFaceConfiguration
import org.junit.Test

class FaceConfigurationTest {

    @Test
    fun `should map correctly the model`() {
        assertThat(protoFaceConfiguration.toDomain()).isEqualTo(faceConfiguration)
        assertThat(faceConfiguration.toProto()).isEqualTo(protoFaceConfiguration)
    }

    @Test
    fun `should map correctly the model with allowedAgeRange missing`() {
        val protoFaceConfigurationWithoutAgeRange = protoFaceConfiguration.toBuilder()
            .setRankOne(protoFaceConfiguration.rankOne.toBuilder().clearAllowedAgeRange())
            .build()

        assertThat(protoFaceConfigurationWithoutAgeRange.toDomain()).isEqualTo(faceConfiguration)
    }

    @Test
    fun `should map correctly the ImageSavingStrategy enums`() {
        val mapping = mapOf(
            ProtoFaceConfiguration.ImageSavingStrategy.NEVER to FaceConfiguration.ImageSavingStrategy.NEVER,
            ProtoFaceConfiguration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE to FaceConfiguration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE,
            ProtoFaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN to FaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
            assertThat(it.value.toProto()).isEqualTo(it.key)
        }
    }
}
