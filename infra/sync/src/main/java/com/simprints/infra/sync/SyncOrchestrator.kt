package com.simprints.infra.sync

import kotlinx.coroutines.flow.Flow

// todo MS-1278 move sync controls into SyncUseCase & its helper usecases, disband the rest into new other usecases
interface SyncOrchestrator {
    suspend fun scheduleBackgroundWork(withDelay: Boolean = false)

    suspend fun cancelBackgroundWork()

    fun startConfigSync()

    /**
     * Trigger project and device configuration sync workers.
     * Emits value when both sync workers are done.
     */
    fun refreshConfiguration(): Flow<Unit>

    suspend fun rescheduleEventSync(withDelay: Boolean = false)

    fun cancelEventSync()

    suspend fun startEventSync(isDownSyncAllowed: Boolean = true)

    fun stopEventSync()

    fun startImageSync()

    fun stopImageSync()

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
