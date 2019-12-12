package com.simprints.id.data.db.syncscope.domain

data class DownSyncInfo(val lastState: DownSyncState,
                        val lastPatientId: String?,
                        val lastPatientUpdatedAt: Long?,
                        val lastSyncTime: Long? = null) {

    enum class DownSyncState {
        RUNNING,
        COMPLETE,
        FAILED
    }
}
