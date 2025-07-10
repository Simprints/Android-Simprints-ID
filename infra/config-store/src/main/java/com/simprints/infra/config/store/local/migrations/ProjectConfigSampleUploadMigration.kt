package com.simprints.infra.config.store.local.migrations

import androidx.datastore.core.DataMigration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.store.local.models.ProtoSampleSynchronizationConfiguration
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2025.3.0
 */
class ProjectConfigSampleUploadMigration @Inject constructor() : DataMigration<ProtoProjectConfiguration> {
    override suspend fun cleanUp() {
        Simber.i("Migration of project configuration sample upload is done", tag = MIGRATION)
    }

    override suspend fun shouldMigrate(currentData: ProtoProjectConfiguration) = with(currentData) {
        val hasNoSamplesSyncObject = !synchronization.hasSamples()
        val eventSyncBatchSizeIsEmpty = synchronization.up.simprints.batchSizes
            .let { it.eventUpSyncs == 0 || it.eventDownSyncs == 0 }

        hasNoSamplesSyncObject || eventSyncBatchSizeIsEmpty
    }

    override suspend fun migrate(currentData: ProtoProjectConfiguration): ProtoProjectConfiguration {
        Simber.i("Start migration of project configuration sample upload", tag = MIGRATION)

        val currentSyncConfig = currentData.synchronization
        val currentBatchSizes = currentSyncConfig.up.simprints.batchSizes
        val upBatchSizes = currentBatchSizes
            .toBuilder()
            .setEventUpSyncs(currentBatchSizes.upSyncs)
            .setEventDownSyncs(currentBatchSizes.downSyncs)
            .setSampleUpSyncs(DEFAULT_BATCH_SIZE)
            .build()
        val upSyncSimprintsConfig = currentSyncConfig.up.simprints
            .toBuilder()
            .setBatchSizes(upBatchSizes)
            .build()
        val upSyncConfig = currentSyncConfig.up
            .toBuilder()
            .setSimprints(upSyncSimprintsConfig)
            .build()
        val samplesConfig = ProtoSampleSynchronizationConfiguration.newBuilder().setSignedUrlBatchSize(DEFAULT_BATCH_SIZE).build()

        return currentData
            .toBuilder()
            .setSynchronization(
                currentSyncConfig
                    .toBuilder()
                    .setUp(upSyncConfig)
                    .setSamples(samplesConfig)
                    .build(),
            ).build()
    }

    companion object {
        private const val DEFAULT_BATCH_SIZE = 1
    }
}
