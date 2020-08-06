package com.simprints.id.data.db.events_sync.up.domain

data class EventUpSyncOperation(val scopeId: String,
                                val queryEvent: LocalEventQuery,
                                val lastState: UpSyncState,
                                val lastSyncTime: Long? = null) {

    enum class UpSyncState {
        RUNNING,
        COMPLETE,
        FAILED
    }
}


