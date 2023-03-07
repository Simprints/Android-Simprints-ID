package com.simprints.id.services.sync.events.master

import androidx.lifecycle.LiveData
import com.simprints.infra.eventsync.status.models.EventSyncState

interface EventSyncStateProcessor {
    fun getLastSyncState(): LiveData<EventSyncState>
}
