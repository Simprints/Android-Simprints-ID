package com.simprints.infra.config.store.local.models

import com.google.common.truth.Truth.*
import com.simprints.infra.config.store.local.models.ProtoDownSynchronizationConfiguration.PartitionType
import com.simprints.infra.config.store.local.models.ProtoUpSynchronizationConfiguration.UpSynchronizationKind
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.Frequency
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
import com.simprints.infra.config.store.testtools.protoSynchronizationConfiguration
import com.simprints.infra.config.store.testtools.synchronizationConfiguration
import org.junit.Test

class SynchronizationConfigurationTest {
    @Test
    fun `should map correctly the model`() {
        assertThat(protoSynchronizationConfiguration.toDomain()).isEqualTo(
            synchronizationConfiguration,
        )
        assertThat(synchronizationConfiguration.toProto()).isEqualTo(
            protoSynchronizationConfiguration,
        )
    }

    @Test
    fun `should map correctly the Frequency enums`() {
        val mapping = mapOf(
            ProtoSyncFrequency.ONLY_PERIODICALLY_UP_SYNC to Frequency.ONLY_PERIODICALLY_UP_SYNC,
            ProtoSyncFrequency.PERIODICALLY to Frequency.PERIODICALLY,
            ProtoSyncFrequency.PERIODICALLY_AND_ON_SESSION_START to Frequency.PERIODICALLY_AND_ON_SESSION_START,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
            assertThat(it.value.toProto()).isEqualTo(it.key)
        }
    }

    @Test
    fun `should map correctly the UpSynchronizationKind enums`() {
        val mapping = mapOf(
            UpSynchronizationKind.NONE to UpSynchronizationConfiguration.UpSynchronizationKind.NONE,
            UpSynchronizationKind.ALL to UpSynchronizationConfiguration.UpSynchronizationKind.ALL,
            UpSynchronizationKind.ONLY_ANALYTICS to UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS,
            UpSynchronizationKind.ONLY_BIOMETRICS to UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
            assertThat(it.value.toProto()).isEqualTo(it.key)
        }
    }

    @Test
    fun `should map correctly the PartitionType enums`() {
        val mapping = mapOf(
            PartitionType.PROJECT to DownSynchronizationConfiguration.PartitionType.PROJECT,
            PartitionType.MODULE to DownSynchronizationConfiguration.PartitionType.MODULE,
            PartitionType.USER to DownSynchronizationConfiguration.PartitionType.USER,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
            assertThat(it.value.toProto()).isEqualTo(it.key)
        }
    }
}
