package com.simprints.id.data.db.people_sync.up.domain

data class PeopleUpSyncOperationResult(val lastState: UpSyncState,
                                       val lastSyncTime: Long? = null) {

    enum class UpSyncState {
        RUNNING,
        COMPLETE,
        FAILED
    }
}
