package com.simprints.id.services.sync.events.down.workers

import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation
import com.simprints.id.services.sync.events.common.WorkerProgressCountReporter
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach

class EventDownSyncDownloaderTaskImpl : EventDownSyncDownloaderTask {

    @ExperimentalCoroutinesApi
    override suspend fun execute(workerId: String,
                                 downSyncOperation: EventDownSyncOperation,
                                 downSyncHelper: EventDownSyncHelper,
                                 syncCache: EventSyncCache,
                                 reporter: WorkerProgressCountReporter,
                                 downloadScope: CoroutineScope): Int {

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
