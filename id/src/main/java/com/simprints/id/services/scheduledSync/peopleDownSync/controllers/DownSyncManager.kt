package com.simprints.id.services.scheduledSync.peopleDownSync.controllers

interface DownSyncManager {
    fun enqueueOneTimeDownSyncMasterWorker()
    fun enqueuePeriodicDownSyncMasterWorker()
    fun dequeueAllSyncWorker()
    fun isDownSyncRunning(): Boolean
}
