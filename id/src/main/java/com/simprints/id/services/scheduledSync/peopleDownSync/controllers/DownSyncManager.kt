package com.simprints.id.services.scheduledSync.peopleDownSync.controllers

import androidx.lifecycle.LiveData

interface DownSyncManager {

    var lastSyncState: LiveData<SyncState?>

    fun sync()
    fun stop()

    fun scheduleSync()
    fun cancelScheduledSync()
}
