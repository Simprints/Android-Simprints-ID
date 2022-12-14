package com.simprints.feature.dashboard.main.sync

import androidx.lifecycle.LiveData
import com.simprints.eventsystem.events_sync.models.EventSyncState

interface EventSyncManager {
    fun getLastSyncState(): LiveData<EventSyncState>

    fun sync()
    fun stop()

    fun scheduleSync()
    fun cancelScheduledSync()
}
