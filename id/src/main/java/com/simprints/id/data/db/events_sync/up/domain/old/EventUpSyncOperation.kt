package com.simprints.id.data.db.events_sync.up.domain.old

import androidx.annotation.Keep
import java.util.*
import  com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation.UpSyncState
@Keep
data class EventUpSyncOperation(val queryEvent: LocalEventQuery,
                                var lastState: UpSyncState? = null,
                                var lastSyncTime: Long? = null) {
}


fun EventUpSyncOperation.getUniqueKey() =
    with(this.queryEvent) {
        UUID.nameUUIDFromBytes(
            "$projectId".toByteArray()
        ).toString()
    }


