package com.simprints.id.services.scheduledSync.peopleDownSync.controllers

import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope

import io.reactivex.Single

interface DownSyncManager {
    fun enqueueOneTimeDownSyncMasterWorker()
    fun buildOneTimeDownSyncMasterWorker(syncScope: SyncScope): OneTimeWorkRequest
    fun enqueuePeriodicDownSyncMasterWorker()
    fun buildPeriodicDownSyncMasterWorker(syncScope: SyncScope): PeriodicWorkRequest
    fun dequeueAllSyncWorker()
    fun isDownSyncRunning(): Single<Boolean>
}
