package com.simprints.infra.sync

interface SyncOrchestrator {

    suspend fun scheduleBackgroundWork()
    suspend fun cancelBackgroundWork()

    fun startDeviceSync()

}
