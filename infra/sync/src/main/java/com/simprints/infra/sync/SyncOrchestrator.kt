package com.simprints.infra.sync

import kotlinx.coroutines.flow.Flow

interface SyncOrchestrator {
    suspend fun scheduleBackgroundWork(withDelay: Boolean = false)

    suspend fun cancelBackgroundWork()

    /**
     * Trigger project and device configuration sync workers.
     * Emits value when both sync workers are done.
     */
    fun refreshConfiguration(): Flow<Unit>

    fun rescheduleEventSync(withDelay: Boolean = false)

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
    fun uploadEnrolmentRecords(
        id: String,
        subjectIds: List<String>,
    )

    suspend fun deleteEventSyncInfo()

    fun cleanupWorkers()
}
