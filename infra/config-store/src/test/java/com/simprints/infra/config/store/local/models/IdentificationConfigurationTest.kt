package com.simprints.infra.config.store.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.IdentificationConfiguration
import com.simprints.infra.config.store.testtools.identificationConfiguration
import com.simprints.infra.config.store.testtools.protoIdentificationConfiguration
import org.junit.Test

class IdentificationConfigurationTest {
    @Test
    fun `should map correctly the model`() {
        assertThat(protoIdentificationConfiguration.toDomain()).isEqualTo(identificationConfiguration)
        assertThat(identificationConfiguration.toProto()).isEqualTo(protoIdentificationConfiguration)
    }

    @Test
    fun `should map correctly the PoolType enums`() {
        val mapping = mapOf(
            ProtoIdentificationConfiguration.PoolType.PROJECT to IdentificationConfiguration.PoolType.PROJECT,
            ProtoIdentificationConfiguration.PoolType.MODULE to IdentificationConfiguration.PoolType.MODULE,
            ProtoIdentificationConfiguration.PoolType.USER to IdentificationConfiguration.PoolType.USER,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
            assertThat(it.value.toProto()).isEqualTo(it.key)
        }
    }
}
