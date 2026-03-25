package com.simprints.infra.config.store.local.migrations

import androidx.datastore.core.DataMigration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2026.2.0
 */
class ProjectConfigDevicesScopeBatchMigration @Inject constructor() : DataMigration<ProtoProjectConfiguration> {
    override suspend fun cleanUp() {
        Simber.i("Migration of project configuration of devices event scope type", tag = MIGRATION)
    }

    override suspend fun shouldMigrate(currentData: ProtoProjectConfiguration): Boolean = with(currentData) {
        return synchronization.up.simprints.batchSizes.devices == 0
    }

    override suspend fun migrate(currentData: ProtoProjectConfiguration): ProtoProjectConfiguration {
        Simber.i("Start migration of devices event scope type", tag = MIGRATION)

        val currentSyncConfig = currentData.synchronization
        val currentBatchSizes = currentSyncConfig.up.simprints.batchSizes
        val upSyncSimprintsConfig = currentSyncConfig.up.simprints
            .toBuilder()
            .setBatchSizes(
                currentBatchSizes
                    .toBuilder()
                    .setDevices(DEFAULT_BATCH_SIZE)
                    .build(),
            ).build()
        val upSyncConfig = currentSyncConfig.up
            .toBuilder()
            .setSimprints(upSyncSimprintsConfig)
            .build()

        return currentData
            .toBuilder()
            .setSynchronization(
                currentSyncConfig
                    .toBuilder()
                    .setUp(upSyncConfig)
                    .build(),
            ).build()
    }

    companion object {
        private const val DEFAULT_BATCH_SIZE = 1
    }
}
