package com.simprints.id.services.scheduledSync.peopleDownSync.controllers

import androidx.lifecycle.LiveData
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncState

interface DownSyncManager {
    fun enqueueOneTimeDownSync()
    fun buildOneTimeDownSyncMasterWorker(syncScope: SyncScope): OneTimeWorkRequest
    fun enqueuePeriodicDownSync()
    fun buildPeriodicDownSyncMasterWorker(syncScope: SyncScope): PeriodicWorkRequest
    fun dequeueAllSyncWorker()
    fun onSyncStateUpdated(): LiveData<SyncState>
}
