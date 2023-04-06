package com.simprints.id.services.sync.events.down.workers

import com.simprints.id.services.sync.events.common.WorkerProgressCountReporter
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
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
