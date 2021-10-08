package com.simprints.id.data.db.events_sync.up.domain.old

import androidx.annotation.Keep
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation.UpSyncState
import java.util.*

@Keep
@Deprecated(message = "This is used to support old data-upload format, should be replaced with new EventUpSyncOperation",
    replaceWith = ReplaceWith(
        expression = "EventUpSyncOperation(input)",
        imports = arrayOf("com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation"))
)
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


