package com.simprints.id.services.scheduledSync.people.master

import androidx.lifecycle.LiveData
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncState

interface PeopleSyncManager {

    fun getLastSyncState(): LiveData<PeopleSyncState>
    fun hasSyncEverRunBefore(): Boolean

    fun sync()
    fun stop()

    fun scheduleSync()
    fun cancelScheduledSync()

    suspend fun deleteSyncInfo()
}
