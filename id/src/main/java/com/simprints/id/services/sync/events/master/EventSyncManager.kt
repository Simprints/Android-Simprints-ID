package com.simprints.id.services.sync.events.master

import androidx.lifecycle.LiveData
import com.simprints.id.services.sync.events.master.models.SubjectsSyncState

interface EventSyncManager {

    fun getLastSyncState(): LiveData<SubjectsSyncState>
    fun hasSyncEverRunBefore(): Boolean

    fun sync()
    fun stop()

    fun scheduleSync()
    fun cancelScheduledSync()

    suspend fun deleteSyncInfo()
}
