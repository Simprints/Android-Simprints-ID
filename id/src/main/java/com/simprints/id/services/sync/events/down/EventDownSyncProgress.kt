package com.simprints.id.services.sync.events.down

import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation


data class EventDownSyncProgress(val operation: EventDownSyncOperation, val progress: Int)
