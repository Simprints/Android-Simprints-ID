package com.simprints.id.data.db.people_sync.up.domain

data class SubjectsUpSyncOperationResult(val lastState: UpSyncState,
                                         val lastSyncTime: Long? = null) {

    enum class UpSyncState {
        RUNNING,
        COMPLETE,
        FAILED
    }
}
