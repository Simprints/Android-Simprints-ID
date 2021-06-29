package com.simprints.id.services.sync.events.down.workers

import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation
import com.simprints.id.services.sync.events.common.WorkerProgressCountReporter
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import kotlinx.coroutines.CoroutineScope

interface EventDownSyncDownloaderTask {

    suspend fun execute(workerId: String,
                        downSyncOperation: com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation,
                        downSyncHelper: EventDownSyncHelper,
                        syncCache: EventSyncCache,
                        reporter: WorkerProgressCountReporter,
                        downloadScope: CoroutineScope): Int
}
