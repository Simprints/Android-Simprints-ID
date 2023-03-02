package com.simprints.id.services.sync.events.master

import androidx.lifecycle.LiveData
import com.simprints.infra.events.events_sync.models.EventSyncState

interface EventSyncStateProcessor {
    fun getLastSyncState(): LiveData<EventSyncState>
}
