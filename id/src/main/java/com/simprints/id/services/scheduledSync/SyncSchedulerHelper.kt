package com.simprints.id.services.scheduledSync

interface SyncSchedulerHelper {
    fun scheduleBackgroundSyncs()
    fun startDownSyncOnLaunchIfPossible()
    fun startDownSyncOnUserActionIfPossible()

    fun cancelAllWorkers()
    fun cancelSessionsSyncWorker()
    fun cancelDownSyncWorkers()

    fun isDownSyncManualTriggerOn(): Boolean
}
