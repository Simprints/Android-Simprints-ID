package com.simprints.infra.config.store.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
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
            ApiSynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC to
                SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC,
            ApiSynchronizationConfiguration.Frequency.PERIODICALLY to SynchronizationConfiguration.Frequency.PERIODICALLY,
            ApiSynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START to
                SynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
        }
    }

    @Test
    fun `should map correctly the UpSynchronizationKind enums`() {
        val mapping = mapOf(
            ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.UpSynchronizationKind.NONE
                to
                UpSynchronizationConfiguration.UpSynchronizationKind.NONE,
            ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.UpSynchronizationKind.ALL
                to
                UpSynchronizationConfiguration.UpSynchronizationKind.ALL,
            ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS
                to
                UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS,
            ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS
                to
                UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
        }
    }

    @Test
    fun `should map correctly the PartitionType enums`() {
        val mapping = mapOf(
            ApiSynchronizationConfiguration.ApiDownSynchronizationConfiguration.PartitionType.PROJECT
                to
                DownSynchronizationConfiguration.PartitionType.PROJECT,
            ApiSynchronizationConfiguration.ApiDownSynchronizationConfiguration.PartitionType.MODULE
                to
                DownSynchronizationConfiguration.PartitionType.MODULE,
            ApiSynchronizationConfiguration.ApiDownSynchronizationConfiguration.PartitionType.USER
                to
                DownSynchronizationConfiguration.PartitionType.USER,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
        }
    }
}
