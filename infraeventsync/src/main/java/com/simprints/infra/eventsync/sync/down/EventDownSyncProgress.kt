package com.simprints.infra.eventsync.sync.down

import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation


internal data class EventDownSyncProgress(val operation: EventDownSyncOperation, val progress: Int)
