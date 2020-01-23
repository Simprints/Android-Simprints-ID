package com.simprints.id.services.scheduledSync.people.master

import androidx.lifecycle.LiveData
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncState

interface PeopleSyncManager {

    fun getLastSyncState(): LiveData<PeopleSyncState>

    fun sync()
    fun stop()

    fun scheduleSync()
    fun cancelScheduledSync()
    fun cancelAndRescheduleSync()
    suspend fun deleteSyncInfo()
}
