package com.simprints.id.services.scheduledSync

interface SyncSchedulerHelper {
    fun scheduleBackgroundSyncs()
    fun startDownSyncOnLaunchIfPossible()
    fun startDownSyncOnUserActionIfPossible()
    fun cancelDownSyncWorkers()
}
