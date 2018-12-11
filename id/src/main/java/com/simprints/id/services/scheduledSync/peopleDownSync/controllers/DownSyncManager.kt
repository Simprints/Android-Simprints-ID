package com.simprints.id.services.scheduledSync.peopleDownSync.controllers

import androidx.lifecycle.LiveData
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncState

import io.reactivex.Single

interface DownSyncManager {
    fun enqueueOneTimeDownSyncMasterWorker()
    fun buildOneTimeDownSyncMasterWorker(syncScope: SyncScope): OneTimeWorkRequest
    fun enqueuePeriodicDownSyncMasterWorker()
    fun buildPeriodicDownSyncMasterWorker(syncScope: SyncScope): PeriodicWorkRequest
    fun dequeueAllSyncWorker()
    fun onSyncStateUpdated(): LiveData<SyncState>
}
