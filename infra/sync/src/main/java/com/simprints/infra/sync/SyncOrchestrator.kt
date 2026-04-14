package com.simprints.infra.sync

import com.simprints.infra.eventsync.status.models.DownSyncState
import com.simprints.infra.eventsync.status.models.UpSyncState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SyncOrchestrator {
    /**
     * A combined reactive stream of sync state for all syncable entities.
     */
    fun observeSyncState(): StateFlow<SyncStatus>

    /**
     * A reactive stream of up-sync state only.
     */
    fun observeUpSyncState(): StateFlow<UpSyncState>

    /**
     * A reactive stream of down-sync state only.
     */
    fun observeDownSyncState(): StateFlow<DownSyncState>

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
