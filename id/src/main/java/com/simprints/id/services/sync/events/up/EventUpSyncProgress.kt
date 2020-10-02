package com.simprints.id.services.sync.events.up

import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation

data class EventUpSyncProgress(val operation: EventUpSyncOperation, val progress: Int)
