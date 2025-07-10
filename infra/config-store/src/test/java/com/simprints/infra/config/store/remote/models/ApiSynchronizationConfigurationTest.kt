package com.simprints.infra.config.store.remote.models

import com.google.common.truth.Truth.*
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.Frequency
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
import com.simprints.infra.config.store.remote.models.ApiSynchronizationConfiguration.ApiSimprintsDownSynchronizationConfiguration
import com.simprints.infra.config.store.testtools.apiSynchronizationConfiguration
import com.simprints.infra.config.store.testtools.synchronizationConfiguration
import org.junit.Test

class ApiSynchronizationConfigurationTest {
    @Test
    fun `should map correctly the model`() {
        assertThat(apiSynchronizationConfiguration.toDomain()).isEqualTo(
            synchronizationConfiguration,
        )
    }

    @Test
    fun `should map correctly the Frequency enums`() {
        val mapping = mapOf(
            ApiSynchronizationConfiguration.ApiSynchronizationFrequency.ONLY_PERIODICALLY_UP_SYNC to Frequency.ONLY_PERIODICALLY_UP_SYNC,
            ApiSynchronizationConfiguration.ApiSynchronizationFrequency.PERIODICALLY to Frequency.PERIODICALLY,
            ApiSynchronizationConfiguration.ApiSynchronizationFrequency.PERIODICALLY_AND_ON_SESSION_START to
                Frequency.PERIODICALLY_AND_ON_SESSION_START,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
        }
    }

    @Test
    fun `should map correctly the UpSynchronizationKind enums`() {
        val mapping = mapOf(
            ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.UpSynchronizationKind.NONE
                to UpSynchronizationConfiguration.UpSynchronizationKind.NONE,
            ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.UpSynchronizationKind.ALL
                to UpSynchronizationConfiguration.UpSynchronizationKind.ALL,
            ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS
                to UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS,
            ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS
                to UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
        }
    }

    @Test
    fun `should map correctly the PartitionType enums`() {
        val mapping = mapOf(
            ApiSimprintsDownSynchronizationConfiguration.PartitionType.PROJECT to DownSynchronizationConfiguration.PartitionType.PROJECT,
            ApiSimprintsDownSynchronizationConfiguration.PartitionType.MODULE to DownSynchronizationConfiguration.PartitionType.MODULE,
            ApiSimprintsDownSynchronizationConfiguration.PartitionType.USER to DownSynchronizationConfiguration.PartitionType.USER,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
        }
    }
}
