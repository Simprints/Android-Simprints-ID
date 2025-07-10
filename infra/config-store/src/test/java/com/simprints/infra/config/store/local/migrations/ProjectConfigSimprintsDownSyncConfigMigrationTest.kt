package com.simprints.infra.config.store.local.migrations

import com.google.common.truth.Truth.*
import com.simprints.infra.config.store.local.models.ProtoDownSynchronizationConfiguration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.store.local.models.ProtoSyncFrequency
import com.simprints.infra.config.store.local.models.ProtoSynchronizationConfiguration
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ProjectConfigSimprintsDownSyncConfigMigrationTest {
    @Test
    fun `should not migrate if has down sync simprints object`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setSynchronization(
                ProtoSynchronizationConfiguration
                    .newBuilder()
                    .setDown(
                        ProtoDownSynchronizationConfiguration
                            .newBuilder()
                            .setSimprints(
                                ProtoDownSynchronizationConfiguration.ProtoSimprintsDownSynchronizationConfiguration
                                    .newBuilder()
                                    .build(),
                            ).build(),
                    ),
            ).build()
        assertThat(ProjectConfigSimprintsSyncConfigMigration().shouldMigrate(currentData)).isFalse()
    }

    @Test
    fun `should migrate if no down sync simprints object`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setSynchronization(
                ProtoSynchronizationConfiguration
                    .newBuilder()
                    .setDown(
                        ProtoDownSynchronizationConfiguration
                            .newBuilder()
                            .clearSimprints()
                            .build(),
                    ),
            ).build()
        assertThat(ProjectConfigSimprintsSyncConfigMigration().shouldMigrate(currentData)).isTrue()
    }

    @Test
    fun `migration moves old down sync values into simprints object`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setSynchronization(
                ProtoSynchronizationConfiguration
                    .newBuilder()
                    .setDown(
                        ProtoDownSynchronizationConfiguration
                            .newBuilder()
                            .clearSimprints()
                            .setMaxAge("PT24H")
                            .setMaxNbOfModules(7)
                            .setPartitionType(ProtoDownSynchronizationConfiguration.PartitionType.MODULE)
                            .addAllModuleOptions(listOf("module1", "module2"))
                            .build(),
                    ),
            ).build()
        val result = ProjectConfigSimprintsSyncConfigMigration().migrate(currentData)

        // Old fields are cleared
        assertThat(result.synchronization.down.maxAge).isEmpty()
        assertThat(result.synchronization.down.maxNbOfModules).isEqualTo(0)
        assertThat(result.synchronization.down.moduleOptionsCount).isEqualTo(0)
        assertThat(result.synchronization.down.partitionType).isNotEqualTo(ProtoDownSynchronizationConfiguration.PartitionType.MODULE)
        //  Data is in the nested fields now
        assertThat(result.synchronization.down.hasSimprints()).isTrue()
        assertThat(result.synchronization.down.simprints.maxAge).isEqualTo("PT24H")
        assertThat(result.synchronization.down.simprints.maxNbOfModules).isEqualTo(7)
        assertThat(result.synchronization.down.simprints.moduleOptionsCount).isEqualTo(2)
        assertThat(
            result.synchronization.down.simprints.partitionType,
        ).isEqualTo(ProtoDownSynchronizationConfiguration.PartitionType.MODULE)
    }

    @Test
    fun `migration moves frequency values into both simprints objects`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setSynchronization(
                ProtoSynchronizationConfiguration
                    .newBuilder()
                    .setFrequency(ProtoSynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START)
                    .setDown(
                        ProtoDownSynchronizationConfiguration
                            .newBuilder()
                            .clearSimprints()
                            .setMaxAge("PT24H")
                            .setMaxNbOfModules(7)
                            .setPartitionType(ProtoDownSynchronizationConfiguration.PartitionType.MODULE)
                            .addAllModuleOptions(listOf("module1", "module2"))
                            .build(),
                    ),
            ).build()
        val result = ProjectConfigSimprintsSyncConfigMigration().migrate(currentData)

        // Old fields are cleared
        assertThat(
            result.synchronization.frequency,
        ).isNotEqualTo(ProtoSynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START)
        assertThat(result.synchronization.down.simprints.frequency).isEqualTo(ProtoSyncFrequency.PERIODICALLY_AND_ON_SESSION_START)
        assertThat(result.synchronization.up.simprints.frequency).isEqualTo(ProtoSyncFrequency.PERIODICALLY_AND_ON_SESSION_START)
    }
}
