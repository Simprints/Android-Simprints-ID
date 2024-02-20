package com.simprints.infra.sync

interface SyncOrchestrator {

    suspend fun scheduleBackgroundWork()
    suspend fun cancelBackgroundWork()

    fun startDeviceSync()

    /**
     * Fully reschedule the background worker.
     * Should be used in when the configuration that affects scheduling has changed.
     */
    suspend fun rescheduleImageUpSync()

    fun cleanupWorkers()
}
