package com.simprints.id.services.scheduledSync.people.master

import androidx.lifecycle.LiveData

interface PeopleSyncManager {

    fun getLastSyncState(): LiveData<PeopleSyncState>

    fun sync()
    fun stop()

    fun scheduleSync()
    fun cancelScheduledSync()
    fun cancelAndRescheduleSync()
}
