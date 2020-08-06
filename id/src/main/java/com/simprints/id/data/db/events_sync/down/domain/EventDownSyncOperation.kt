package com.simprints.id.data.db.events_sync.down.domain


data class EventDownSyncOperation(val scopeId: String,
                                  val queryEvent: RemoteEventQuery,
                                  val state: DownSyncState,
                                  val lastEventId: String? = null,
                                  val lastSyncTime: Long? = null) {

    enum class DownSyncState {
        RUNNING,
        COMPLETE,
        FAILED
    }
}
