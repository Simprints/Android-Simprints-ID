package com.simprints.infra.eventsync.sync.up

import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation

internal data class EventUpSyncProgress(
    val operation: EventUpSyncOperation,
    val progress: Int,
)
