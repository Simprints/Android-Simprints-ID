package com.simprints.infra.config.store.local.migrations

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.store.local.models.ProtoSampleSynchronizationConfiguration
import com.simprints.infra.config.store.local.models.ProtoSynchronizationConfiguration
import com.simprints.infra.config.store.local.models.ProtoUpSyncBatchSizes
import com.simprints.infra.config.store.local.models.ProtoUpSynchronizationConfiguration
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ProjectConfigSampleUploadMigrationTest {
    @Test
    fun `should not migrate if sync config has samples object`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setSynchronization(
                ProtoSynchronizationConfiguration
                    .newBuilder()
                    .setSamples(ProtoSampleSynchronizationConfiguration.newBuilder().build())
                    .setUp(
                        ProtoUpSynchronizationConfiguration
                            .newBuilder()
                            .setSimprints(
                                ProtoUpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration
                                    .newBuilder()
                                    .setBatchSizes(
                                        ProtoUpSyncBatchSizes
                                            .newBuilder()
                                            .setEventUpSyncs(1)
                                            .setEventDownSyncs(1)
                                            .build(),
                                    ).build(),
                            ).build(),
                    ).build(),
            ).build()
        assertThat(ProjectConfigSampleUploadMigration().shouldMigrate(currentData)).isFalse()
    }

    @Test
    fun `should migrate if sync config has no samples config`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setSynchronization(
                ProtoSynchronizationConfiguration
                    .newBuilder()
                    .setSamples(ProtoSampleSynchronizationConfiguration.newBuilder().build())
                    .setUp(
                        ProtoUpSynchronizationConfiguration
                            .newBuilder()
                            .setSimprints(
                                ProtoUpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration
                                    .newBuilder()
                                    .setBatchSizes(
                                        ProtoUpSyncBatchSizes
                                            .newBuilder()
                                            .setUpSyncs(1)
                                            .setDownSyncs(1)
                                            .build(),
                                    ).build(),
                            ).build(),
                    ).build(),
            ).build()

        assertThat(ProjectConfigSampleUploadMigration().shouldMigrate(currentData)).isTrue()
    }

    @Test
    fun `should migrate if sync config has no events upsync config`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setSynchronization(
                ProtoSynchronizationConfiguration
                    .newBuilder()
                    .clearSamples()
                    .build(),
            ).build()

        assertThat(ProjectConfigSampleUploadMigration().shouldMigrate(currentData)).isTrue()
    }

    @Test
    fun `should add default values for samples config`() = runTest {
        val currentData = ProtoProjectConfiguration
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
                                    .setBatchSizes(
                                        ProtoUpSyncBatchSizes
                                            .newBuilder()
                                            .setUpSyncs(1)
                                            .setDownSyncs(1)
                                            .build(),
                                    ).build(),
                            ).build(),
                    ).build(),
            ).build()

        val result = ProjectConfigSampleUploadMigration().migrate(currentData)

        assertThat(result.synchronization.hasSamples()).isTrue()
        assertThat(result.synchronization.samples?.signedUrlBatchSize).isEqualTo(1)
        assertThat(result.synchronization.up.simprints.batchSizes.sampleUpSyncs).isEqualTo(1)
        assertThat(result.synchronization.up.simprints.batchSizes.eventUpSyncs).isEqualTo(1)
        assertThat(result.synchronization.up.simprints.batchSizes.eventDownSyncs).isEqualTo(1)
    }
}
