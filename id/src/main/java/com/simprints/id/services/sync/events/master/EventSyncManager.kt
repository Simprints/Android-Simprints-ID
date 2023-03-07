package com.simprints.id.services.sync.events.master

import androidx.lifecycle.LiveData
import com.simprints.infra.eventsync.status.models.EventSyncState

interface EventSyncManager {

    fun getLastSyncState(): LiveData<EventSyncState>
    fun hasSyncEverRunBefore(): Boolean

    fun sync()
    fun stop()

    fun scheduleSync()
    fun cancelScheduledSync()

    suspend fun deleteSyncInfo()
}
