package com.simprints.infra.sync

interface SyncOrchestrator {

    suspend fun scheduleBackgroundWork()
    suspend fun cancelBackgroundWork()

    fun startProjectSync()
    fun startDeviceSync()

    fun rescheduleEventSync()
    fun cancelEventSync()
    fun startEventSync()
    fun stopEventSync()

    /**
     * Fully reschedule the background worker.
     * Should be used in when the configuration that affects scheduling has changed.
     */
    suspend fun rescheduleImageUpSync()

    /**
     * Schedule a worker to upload subjects with IDs in the provided list.
     */
    fun uploadEnrolmentRecords(id: String, subjectIds: List<String>)

    suspend fun deleteEventSyncInfo()
    fun cleanupWorkers()
}
