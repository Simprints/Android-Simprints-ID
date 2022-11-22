package com.simprints.eventsystem.events_sync.models

import androidx.annotation.Keep

@Keep
data class EventSyncState(val syncId: String,
                          val progress: Int,
                          val total: Int?,
                          val upSyncWorkersInfo: List<SyncWorkerInfo>,
                          val downSyncWorkersInfo: List<SyncWorkerInfo>) {

    data class SyncWorkerInfo(val type: EventSyncWorkerType,
                              val state: EventSyncWorkerState
    )

}
