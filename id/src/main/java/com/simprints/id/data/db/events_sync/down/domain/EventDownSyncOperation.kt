package com.simprints.id.data.db.events_sync.down.domain

import java.util.*

data class EventDownSyncOperation(val scopeId: String,
                                  val queryEvent: RemoteEventQuery,
                                  val state: DownSyncState? = null,
                                  val lastEventId: String? = null,
                                  val lastSyncTime: Long? = null) {

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
                "$attendantId" +
                "$subjectId" +
                "${moduleIds?.joinToString()}" +
                modes.joinToString { it.name } +
                types.joinToString { it.name }
                ).toByteArray()
        ).toString()
    }
