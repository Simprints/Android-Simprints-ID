package com.simprints.infra.config.store.local.migrations

import androidx.datastore.core.DataMigration
import com.simprints.infra.config.store.local.models.ProtoDownSynchronizationConfiguration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.store.local.models.ProtoSyncFrequency
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2025.3.0
 */
class ProjectConfigSimprintsSyncConfigMigration @Inject constructor() : DataMigration<ProtoProjectConfiguration> {
    override suspend fun cleanUp() {
        Simber.i("Migration of project configuration simprints sync is done", tag = MIGRATION)
    }

    override suspend fun shouldMigrate(currentData: ProtoProjectConfiguration) = with(currentData) {
        !synchronization.down.hasSimprints()
    }

    override suspend fun migrate(currentData: ProtoProjectConfiguration): ProtoProjectConfiguration {
        Simber.i("Start migration of project configuration simprints sync", tag = MIGRATION)

        val currentSyncFrequency = ProtoSyncFrequency.forNumber(currentData.synchronization.frequency.number)

        val currentDownSyncConfig = currentData.synchronization.down
        val newDownSyncConfig = currentDownSyncConfig
            .toBuilder()
            .setSimprints(
                ProtoDownSynchronizationConfiguration.ProtoSimprintsDownSynchronizationConfiguration
                    .newBuilder()
                    .setPartitionType(currentDownSyncConfig.partitionType)
                    .setMaxNbOfModules(currentDownSyncConfig.maxNbOfModules)
                    .addAllModuleOptions(currentDownSyncConfig.moduleOptionsList)
                    .setIsTokenized(currentDownSyncConfig.isTokenized)
                    .setMaxAge(currentDownSyncConfig.maxAge)
                    .setFrequency(currentSyncFrequency)
                    .build(),
            ).clearPartitionType()
            .clearMaxNbOfModules()
            .clearModuleOptions()
            .clearIsTokenized()
            .clearMaxAge()
            .build()

        val newUpSyncConfig = currentData.synchronization.up
            .toBuilder()
            .setSimprints(
                currentData.synchronization.up.simprints
                    .toBuilder()
                    .setFrequency(currentSyncFrequency)
                    .build(),
            ).build()

        return currentData
            .toBuilder()
            .setSynchronization(
                currentData.synchronization
                    .toBuilder()
                    .setDown(newDownSyncConfig)
                    .setUp(newUpSyncConfig)
                    .clearFrequency() // Moved to simprints blocks
                    .build(),
            ).build()
    }
}
