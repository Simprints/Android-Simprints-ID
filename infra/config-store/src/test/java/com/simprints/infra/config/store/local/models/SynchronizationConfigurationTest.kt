package com.simprints.infra.config.store.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.SynchronizationConfiguration
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
            ProtoSynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC to
                SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC,
            ProtoSynchronizationConfiguration.Frequency.PERIODICALLY to SynchronizationConfiguration.Frequency.PERIODICALLY,
            ProtoSynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START to
                SynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
            assertThat(it.value.toProto()).isEqualTo(it.key)
        }
    }

    @Test
    fun `should map correctly the UpSynchronizationKind enums`() {
        val mapping = mapOf(
            ProtoUpSynchronizationConfiguration.UpSynchronizationKind.NONE
                to
                UpSynchronizationConfiguration.UpSynchronizationKind.NONE,
            ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ALL
                to
                UpSynchronizationConfiguration.UpSynchronizationKind.ALL,
            ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS
                to
                UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS,
            ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS
                to
                UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
            assertThat(it.value.toProto()).isEqualTo(it.key)
        }
    }

    @Test
    fun `should map correctly the PartitionType enums`() {
        val mapping = mapOf(
            ProtoDownSynchronizationConfiguration.PartitionType.PROJECT
                to
                DownSynchronizationConfiguration.PartitionType.PROJECT,
            ProtoDownSynchronizationConfiguration.PartitionType.MODULE
                to
                DownSynchronizationConfiguration.PartitionType.MODULE,
            ProtoDownSynchronizationConfiguration.PartitionType.USER
                to
                DownSynchronizationConfiguration.PartitionType.USER,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
            assertThat(it.value.toProto()).isEqualTo(it.key)
        }
    }
}
