package com.simprints.id.data.db.people_sync.down.domain

data class SubjectsDownSyncOperationResult(val state: DownSyncState,
                                           val lastEventId: String? = null,
                                           val lastSyncTime: Long? = null) {

    enum class DownSyncState {
        RUNNING,
        COMPLETE,
        FAILED
    }
}
