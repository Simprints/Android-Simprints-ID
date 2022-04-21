package com.simprints.id.services.sync.events.up

data class EventUpSyncProgress(val operation: com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation, val progress: Int)
