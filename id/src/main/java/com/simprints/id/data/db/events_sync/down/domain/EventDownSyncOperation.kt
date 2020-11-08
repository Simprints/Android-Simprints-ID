package com.simprints.id.data.db.events_sync.down.domain

import androidx.annotation.Keep
import java.util.*

@Keep
data class EventDownSyncOperation(val queryEvent: RemoteEventQuery,
                                  val state: DownSyncState? = null,
                                  val lastEventId: String? = null,
                                  val lastSyncTime: Long? = null) {

    @Keep
    enum class DownSyncState {
        RUNNING,
        COMPLETE,
        FAILED
    }
}

//Unique key: all request params expect for lastEventId
fun EventDownSyncOperation.getUniqueKey(): String =
    with(this.queryEvent) {
        UUID.nameUUIDFromBytes(
            (projectId +
                (attendantId ?: "") +
                (subjectId ?: "") +
                ((moduleIds ?: emptyList()).joinToString()) +
                modes.joinToString { it.name } +
                types.joinToString { it.name }
                ).toByteArray()
        ).toString()
    }
