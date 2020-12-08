package com.simprints.id.data.db.events_sync.up.domain

import androidx.annotation.Keep
import java.util.*

@Keep
data class EventUpSyncOperation(val queryEvent: LocalEventQuery,
                                var lastState: UpSyncState? = null,
                                var lastSyncTime: Long? = null) {
    @Keep
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
