package com.simprints.infra.config.store.local.migrations

import com.google.common.truth.Truth.*
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.store.local.models.ProtoSynchronizationConfiguration
import com.simprints.infra.config.store.local.models.ProtoUpSyncBatchSizes
import com.simprints.infra.config.store.local.models.ProtoUpSynchronizationConfiguration
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ProjectConfigDevicesScopeBatchMigrationTest {
    @Test
    fun `should not migrate if sync config has devices batches`() = runTest {
        val currentData = protoConfigWithBatchSizes(
            ProtoUpSyncBatchSizes.newBuilder().setDevices(1).build(),
        )
        assertThat(ProjectConfigDevicesScopeBatchMigration().shouldMigrate(currentData)).isFalse()
    }

    @Test
    fun `should migrate if sync config has no devices batch config`() = runTest {
        val currentData = protoConfigWithBatchSizes(
            ProtoUpSyncBatchSizes.newBuilder().build(),
        )
        assertThat(ProjectConfigDevicesScopeBatchMigration().shouldMigrate(currentData)).isTrue()
    }

    @Test
    fun `should add default values for devices batch config`() = runTest {
        val currentData = protoConfigWithBatchSizes(
            ProtoUpSyncBatchSizes.newBuilder().build(),
        )

        val result = ProjectConfigDevicesScopeBatchMigration().migrate(currentData)
        assertThat(result.synchronization.up.simprints.batchSizes.devices).isEqualTo(1)
    }

    private fun protoConfigWithBatchSizes(batchSize: ProtoUpSyncBatchSizes): ProtoProjectConfiguration = ProtoProjectConfiguration
        .newBuilder()
        .setSynchronization(
            ProtoSynchronizationConfiguration
                .newBuilder()
                .clearSamples()
                .setUp(
                    ProtoUpSynchronizationConfiguration
                        .newBuilder()
                        .setSimprints(
                            ProtoUpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration
                                .newBuilder()
                                .setBatchSizes(batchSize)
                                .build(),
                        ).build(),
                ).build(),
        ).build()
}
