package com.simprints.infra.eventsync.sync.down

import com.simprints.infra.eventsync.status.models.DownSyncCounts

interface EventDownSyncCountsRepository {
    /**
     * Non-reactive by design: the return value has no source that is directly trackable reactively.
     */
    suspend fun countEventsToDownload(): DownSyncCounts
}
