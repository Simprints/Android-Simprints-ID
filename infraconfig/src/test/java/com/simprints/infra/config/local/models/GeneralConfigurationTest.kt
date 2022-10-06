package com.simprints.infra.config.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.testtools.generalConfiguration
import com.simprints.infra.config.testtools.protoGeneralConfiguration
import org.junit.Test

class GeneralConfigurationTest {

    @Test
    fun `should map correctly the model`() {
        assertThat(protoGeneralConfiguration.toDomain()).isEqualTo(generalConfiguration)
        assertThat(generalConfiguration.toProto()).isEqualTo(protoGeneralConfiguration)
    }

    @Test
    fun `should map correctly the Modality enums`() {
        val mapping = mapOf(
            ProtoGeneralConfiguration.Modality.FACE to GeneralConfiguration.Modality.FACE,
            ProtoGeneralConfiguration.Modality.FINGERPRINT to GeneralConfiguration.Modality.FINGERPRINT,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
            assertThat(it.value.toProto()).isEqualTo(it.key)
        }
    }
}
