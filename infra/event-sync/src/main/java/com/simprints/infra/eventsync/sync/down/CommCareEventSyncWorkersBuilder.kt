package com.simprints.infra.eventsync.sync.down

import androidx.work.Constraints
import androidx.work.OneTimeWorkRequest
import com.simprints.core.domain.common.Partitioning
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.sync.down.workers.BaseEventDownSyncDownloaderWorker
import com.simprints.infra.eventsync.sync.down.workers.CommCareEventSyncDownloaderWorker
import javax.inject.Inject

internal class CommCareEventSyncWorkersBuilder @Inject constructor(
    downSyncScopeRepository: EventDownSyncScopeRepository,
    jsonHelper: JsonHelper,
    configManager: ConfigManager,
) : BaseEventDownSyncWorkersBuilder(
        downSyncScopeRepository,
        jsonHelper,
        configManager,
    ) {
    override fun getWorkerClass(): Class<out BaseEventDownSyncDownloaderWorker> = CommCareEventSyncDownloaderWorker::class.java

    override fun getDownSyncWorkerConstraints() = Constraints
        .Builder()
        .build()

    override suspend fun buildDownSyncWorkerChain(
        uniqueSyncId: String,
        uniqueDownSyncId: String,
    ): List<OneTimeWorkRequest> {
        val projectConfiguration = configManager.getProjectConfiguration()

        val downSyncScope = downSyncScopeRepository.getDownSyncScope(
            modes = projectConfiguration.general.modalities,
            selectedModuleIDs = emptyList(),
            syncPartitioning = Partitioning.GLOBAL,
        )

        return downSyncScope.operations.map { downSyncOperation ->
            buildDownSyncWorkers(uniqueSyncId, uniqueDownSyncId, downSyncOperation)
        }
    }
}
