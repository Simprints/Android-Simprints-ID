package com.simprints.id.data.db.events_sync.down.domain

data class EventDownSyncOperation(val scopeId: String,
                                  val queryEvent: RemoteEventQuery,
                                  val state: DownSyncState? = null,
                                  val lastEventId: String? = null,
                                  val lastSyncTime: Long? = null) {

    enum class DownSyncState {
        RUNNING,
        COMPLETE,
        FAILED
    }
}

//Unique key: all request params expect for lastEventId
fun EventDownSyncOperation.getUniqueKey(): Int {
    val paramsRequest = this.queryEvent.copy(lastEventId = null)
    return paramsRequest.hashCode()
}

