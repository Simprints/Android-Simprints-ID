package com.simprints.feature.dashboard.sync

import androidx.lifecycle.LiveData
import com.simprints.eventsystem.events_sync.models.EventSyncState

interface EventSyncManager {
    fun sync()
    fun getLastSyncState(): LiveData<EventSyncState>
}
