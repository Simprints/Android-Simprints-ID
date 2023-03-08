package com.simprints.infra.eventsync.sync.down

import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.WorkerProgressCountReporter
import kotlinx.coroutines.CoroutineScope

interface EventDownSyncDownloaderTask {

    suspend fun execute(
        workerId: String,
        downSyncOperation: EventDownSyncOperation,
        downSyncHelper: EventDownSyncHelper,
        syncCache: EventSyncCache,
        reporter: WorkerProgressCountReporter,
        downloadScope: CoroutineScope
    ): Int
}
