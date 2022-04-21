package com.simprints.id.services.sync.events.down

data class EventDownSyncProgress(val operation: com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation, val progress: Int)
