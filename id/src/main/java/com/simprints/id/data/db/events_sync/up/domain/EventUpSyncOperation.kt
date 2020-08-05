package com.simprints.id.data.db.events_sync.up.domain

data class EventUpSyncOperation(val projectId: String,
                                val lastResult: Result?) {

    data class Result(val lastState: UpSyncState,
                      val lastSyncTime: Long? = null) {

        enum class UpSyncState {
            RUNNING,
            COMPLETE,
            FAILED
        }
    }
}


