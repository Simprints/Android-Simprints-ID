package com.simprints.id.services.scheduledSync.people.master

import androidx.annotation.Keep
import androidx.work.WorkInfo

@Keep
class PeopleSyncState(val syncId: String,
                      val progress: Int,
                      val total: Int?,
                      val upSyncStates: List<WorkerState>,
                      val downSyncStates: List<WorkerState>) {

    enum class SyncState {
        ENQUEUED,
        RUNNING,
        COMPLETED,
        FAILED;
    }

    data class WorkerState(val type: PeopleSyncWorkerType, val state: WorkInfo.State)
}
