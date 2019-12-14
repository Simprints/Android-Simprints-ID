package com.simprints.id.data.db.people_sync.down.domain

data class PeopleDownSyncOperationResult(val lastState: DownSyncState,
                                         val lastPatientId: String?,
                                         val lastPatientUpdatedAt: Long?,
                                         val lastSyncTime: Long? = null) {

    enum class DownSyncState {
        RUNNING,
        COMPLETE,
        FAILED
    }
}
