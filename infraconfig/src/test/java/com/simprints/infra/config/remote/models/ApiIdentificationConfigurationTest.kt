package com.simprints.infra.config.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.domain.IdentificationConfiguration
import com.simprints.infra.config.testtools.apiIdentificationConfiguration
import com.simprints.infra.config.testtools.identificationConfiguration
import org.junit.Test

class ApiIdentificationConfigurationTest {

    @Test
    fun `should map correctly the model`() {
        assertThat(apiIdentificationConfiguration.toDomain()).isEqualTo(identificationConfiguration)
    }

    @Test
    fun `should map correctly the PoolType enums`() {
        val mapping = mapOf(
            ApiIdentificationConfiguration.PoolType.PROJECT to IdentificationConfiguration.PoolType.PROJECT,
            ApiIdentificationConfiguration.PoolType.MODULE to IdentificationConfiguration.PoolType.MODULE,
            ApiIdentificationConfiguration.PoolType.USER to IdentificationConfiguration.PoolType.USER,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
        }
    }
}
