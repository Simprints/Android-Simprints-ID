package com.simprints.infra.eventsync.sync.down

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import com.simprints.core.domain.tokenization.values
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.sync.down.workers.BaseEventDownSyncDownloaderWorker
import com.simprints.infra.eventsync.sync.down.workers.SimprintsEventDownSyncDownloaderWorker
import javax.inject.Inject

internal class SimprintsEventDownSyncWorkersBuilder @Inject constructor(
    downSyncScopeRepository: EventDownSyncScopeRepository,
    configRepository: ConfigRepository,
) : BaseEventDownSyncWorkersBuilder(
        downSyncScopeRepository,
        configRepository,
    ) {
    override fun getWorkerClass(): Class<out BaseEventDownSyncDownloaderWorker> = SimprintsEventDownSyncDownloaderWorker::class.java

    override fun getDownSyncWorkerConstraints() = Constraints
        .Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    override suspend fun buildDownSyncWorkerChain(
        uniqueSyncId: String,
        uniqueDownSyncId: String,
    ): List<OneTimeWorkRequest> {
        val projectConfiguration = configRepository.getProjectConfiguration()
        val deviceConfiguration = configRepository.getDeviceConfiguration()

        val downSyncScope = downSyncScopeRepository.getDownSyncScope(
            modes = projectConfiguration.general.modalities,
            selectedModuleIDs = deviceConfiguration.selectedModules.values(),
            syncPartitioning = projectConfiguration.synchronization.down.simprints!!
                .partitionType
                .toDomain(),
        )

        return downSyncScope.operations.map { downSyncOperation ->
            buildDownSyncWorkers(uniqueSyncId, uniqueDownSyncId, downSyncOperation)
        }
    }
}
