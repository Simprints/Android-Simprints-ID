package com.simprints.infra.eventsync.sync.common

import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation

internal data class EventDownSyncProgress(
    val operation: EventDownSyncOperation,
    val progress: Int,
    val maxProgress: Int?,
)
