package com.simprints.infra.sync

import kotlinx.coroutines.flow.Flow

// todo MS-1300 disband into usecases
interface SyncOrchestrator {
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
