package com.simprints.eventsystem.events_sync.down.domain

import androidx.annotation.Keep
import java.util.*

@Keep
data class EventDownSyncOperation(
    val queryEvent: RemoteEventQuery,
    val state: DownSyncState? = null,
    val lastEventId: String? = null,
    val lastSyncTime: Long? = null
) {

    @Keep
    enum class DownSyncState {
        RUNNING,
        COMPLETE,
        FAILED
    }
}

// We need to keep this old types otherwise the unique key of the down-sync will change
// and we will need to down-sync again from scratch.
internal var oldTypes =
    "ENROLMENT_RECORD_CREATION, ENROLMENT_RECORD_MOVE, ENROLMENT_RECORD_DELETION"

//Unique key: all request params expect for lastEventId
fun EventDownSyncOperation.getUniqueKey(): String =
    with(this.queryEvent) {
        UUID.nameUUIDFromBytes(
            (projectId +
                (attendantId ?: "") +
                (subjectId ?: "") +
                ((moduleIds ?: emptyList()).joinToString()) +
                modes.joinToString { it.name } +
                oldTypes
                ).toByteArray()
        ).toString()
    }
