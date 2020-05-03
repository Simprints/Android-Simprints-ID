package com.simprints.id.services.scheduledSync.subjects.master

import androidx.lifecycle.LiveData
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncState

interface SubjectsSyncManager {

    fun getLastSyncState(): LiveData<SubjectsSyncState>
    fun hasSyncEverRunBefore(): Boolean

    fun sync()
    fun stop()

    fun scheduleSync()
    fun cancelScheduledSync()

    suspend fun deleteSyncInfo()
}
