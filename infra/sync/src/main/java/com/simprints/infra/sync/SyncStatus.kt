package com.simprints.infra.sync

import com.simprints.infra.eventsync.status.models.DownSyncState
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.status.models.UpSyncState

data class SyncStatus(
    val upSyncState: UpSyncState,
    val downSyncState: DownSyncState,
    val imageSyncStatus: ImageSyncStatus,
) {
    @Deprecated(
        "Use upSyncState and downSyncState separately",
        ReplaceWith("upSyncState or downSyncState"),
    )
    val eventSyncState: EventSyncState
        get() = EventSyncState(
            syncId = upSyncState.syncId.ifBlank { downSyncState.syncId },
            progress = combineNullable(upSyncState.progress, downSyncState.progress),
            total = combineNullable(upSyncState.total, downSyncState.total),
            upSyncWorkersInfo = upSyncState.workersInfo,
            downSyncWorkersInfo = downSyncState.workersInfo,
            reporterStates = emptyList(),
            lastSyncTime = listOfNotNull(upSyncState.lastSyncTime, downSyncState.lastSyncTime).maxOrNull(),
        )

    private fun combineNullable(a: Int?, b: Int?): Int? = when {
        a == null || b == null -> null
        else -> a + b
    }
}
