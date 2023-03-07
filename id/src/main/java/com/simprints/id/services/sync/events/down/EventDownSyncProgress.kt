package com.simprints.id.services.sync.events.down

import com.simprints.infra.events.events_sync.down.domain.EventDownSyncOperation

data class EventDownSyncProgress(val operation: EventDownSyncOperation, val progress: Int)
