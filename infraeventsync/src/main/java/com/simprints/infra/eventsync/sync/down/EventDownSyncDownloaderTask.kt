package com.simprints.infra.eventsync.sync.down

import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.WorkerProgressCountReporter
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

internal class EventDownSyncDownloaderTask @Inject constructor() {

    suspend fun execute(
        workerId: String,
        downSyncOperation: EventDownSyncOperation,
        downSyncHelper: EventDownSyncHelper,
        syncCache: EventSyncCache,
        reporter: WorkerProgressCountReporter,
        downloadScope: CoroutineScope
    ): Int {

        var progress = syncCache.readProgress(workerId)
        val totalDownloaded = downSyncHelper.downSync(downloadScope, downSyncOperation)

        totalDownloaded.collect {
            progress = it.progress
            syncCache.saveProgress(workerId, progress)
            reporter.reportCount(progress)
        }

        return progress
    }
}
