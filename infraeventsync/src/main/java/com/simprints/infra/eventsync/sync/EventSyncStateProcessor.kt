package com.simprints.infra.eventsync.sync

import androidx.lifecycle.LiveData
import com.simprints.infra.eventsync.status.models.EventSyncState

interface EventSyncStateProcessor {
    fun getLastSyncState(): LiveData<EventSyncState>
}
