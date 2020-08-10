package com.simprints.id.data.db.events_sync.up.domain

data class EventUpSyncOperation(val scopeId: String,
                                val queryEvent: LocalEventQuery,
                                var lastState: UpSyncState? = null,
                                var lastSyncTime: Long? = null) {

    enum class UpSyncState {
        RUNNING,
        COMPLETE,
        FAILED
    }
}

fun EventUpSyncOperation.getUniqueKey() =
    this.copy(lastState = null, lastSyncTime = null).hashCode()
