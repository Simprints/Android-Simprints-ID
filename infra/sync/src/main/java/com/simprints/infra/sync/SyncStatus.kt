package com.simprints.infra.sync

import com.simprints.infra.eventsync.status.models.EventSyncState

data class SyncStatus(
    val eventSyncState: EventSyncState,
    val imageSyncStatus: ImageSyncStatus,
)
