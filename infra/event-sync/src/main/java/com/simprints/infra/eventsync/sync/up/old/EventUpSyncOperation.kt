package com.simprints.infra.eventsync.sync.up.old

import androidx.annotation.Keep
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation.UpSyncState

@Keep
@Deprecated(
    message = "This is used to support old data-upload format, should be replaced with new EventUpSyncOperation",
    replaceWith = ReplaceWith(
        expression = "EventUpSyncOperation(input)",
        imports = arrayOf("com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation"),
    ),
)
internal data class EventUpSyncOperation(
    val queryEvent: LocalEventQuery,
    var lastState: UpSyncState? = null,
    var lastSyncTime: Long? = null,
)
