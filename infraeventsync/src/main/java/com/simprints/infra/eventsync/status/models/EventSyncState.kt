package com.simprints.infra.eventsync.status.models

import androidx.annotation.Keep

@Keep
data class EventSyncState(
    val syncId: String,
    val progress: Int,
    val total: Int?,
    val upSyncWorkersInfo: List<SyncWorkerInfo>,
    val downSyncWorkersInfo: List<SyncWorkerInfo>,
) {

    data class SyncWorkerInfo(
        val type: EventSyncWorkerType,
        val state: EventSyncWorkerState,
    )

    fun isSyncRunning(): Boolean = (upSyncWorkersInfo + downSyncWorkersInfo)
        .any { it.state is EventSyncWorkerState.Running || it.state is EventSyncWorkerState.Enqueued }
}
