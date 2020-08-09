package com.simprints.id.data.db.events_sync.down.domain


data class EventDownSyncOperation(val scopeId: String,
                                  var queryEvent: RemoteEventQuery,
                                  var state: DownSyncState? = null,
                                  var lastEventId: String? = null,
                                  var lastSyncTime: Long? = null) {

    enum class DownSyncState {
        RUNNING,
        COMPLETE,
        FAILED
    }
}
