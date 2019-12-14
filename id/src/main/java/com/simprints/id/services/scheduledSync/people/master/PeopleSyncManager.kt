package com.simprints.id.services.scheduledSync.people.master

import androidx.lifecycle.LiveData
import com.simprints.id.services.scheduledSync.people.down.models.SyncState

interface PeopleSyncManager {

    var lastSyncState: LiveData<SyncState?>

    fun sync()
    fun stop()

    fun scheduleSync()
    fun cancelScheduledSync()
}
