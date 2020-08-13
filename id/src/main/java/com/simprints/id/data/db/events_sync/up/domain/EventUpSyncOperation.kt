package com.simprints.id.data.db.events_sync.up.domain

import java.util.*

data class EventUpSyncOperation(val queryEvent: LocalEventQuery,
                                var lastState: UpSyncState? = null,
                                var lastSyncTime: Long? = null) {

    enum class UpSyncState {
        RUNNING,
        COMPLETE,
        FAILED
    }
}

fun EventUpSyncOperation.getUniqueKey() =
    with(this.queryEvent) {
        UUID.nameUUIDFromBytes(
            "$projectId".toByteArray()
        ).toString()
    }
