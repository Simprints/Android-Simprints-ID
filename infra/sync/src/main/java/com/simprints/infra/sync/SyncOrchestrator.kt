package com.simprints.infra.sync

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SyncOrchestrator {
    /**
     * A combined reactive stream of sync state for all syncable entities.
     */
    fun observeSyncState(): StateFlow<SyncStatus>

    /**
     * Executes an immediate (one-time) sync control command.
     * Returns a job of the ongoing command execution.
     */
    fun execute(command: OneTime): Job

    /**
     * Executes a periodic/background scheduling command.
     * Returns a job of the ongoing command execution.
     */
    fun execute(command: ScheduleCommand): Job

    fun startConfigSync()

    /**
     * Trigger project and device configuration sync workers.
     * Emits value when both sync workers are done.
     */
    fun refreshConfiguration(): Flow<Unit>

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
