package com.simprints.infra.eventsync.sync.down

import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.WorkerProgressCountReporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.consumeEach
import javax.inject.Inject

class EventDownSyncDownloaderTaskImpl @Inject constructor(): EventDownSyncDownloaderTask {

    override suspend fun execute(
        workerId: String,
        downSyncOperation: EventDownSyncOperation,
        downSyncHelper: EventDownSyncHelper,
        syncCache: EventSyncCache,
        reporter: WorkerProgressCountReporter,
        downloadScope: CoroutineScope
    ): Int {

        var progress = syncCache.readProgress(workerId)
        val totalDownloaded = downSyncHelper.downSync(downloadScope, downSyncOperation)

        totalDownloaded.consumeEach {
            progress = it.progress
            syncCache.saveProgress(workerId, progress)
            reporter.reportCount(progress)
        }

        return progress
    }
}
