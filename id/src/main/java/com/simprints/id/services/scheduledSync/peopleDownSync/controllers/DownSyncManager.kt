package com.simprints.id.services.scheduledSync.peopleDownSync.controllers

import io.reactivex.Single

interface DownSyncManager {
    fun enqueueOneTimeDownSyncMasterWorker()
    fun enqueuePeriodicDownSyncMasterWorker()
    fun dequeueAllSyncWorker()
    fun isDownSyncRunning(): Single<Boolean>
}
