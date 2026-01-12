package com.simprints.infra.eventsync.status.down.domain

import androidx.annotation.Keep
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
internal data class EventDownSyncOperation(
    val queryEvent: RemoteEventQuery,
    val state: DownSyncState? = null,
    val lastEventId: String? = null,
    val lastSyncTime: Long? = null,
) {
    @Keep
    @Serializable
    enum class DownSyncState {
        RUNNING,
        COMPLETE,
        FAILED,
    }

    // Unique key: all request params expect for lastEventId
    internal fun getUniqueKey(): String = with(this.queryEvent) {
        listOfNotNull(projectId, attendantId, subjectId, moduleId)
            .joinToString(separator = "")
            .toByteArray()
            .let { UUID.nameUUIDFromBytes(it).toString() }
    }
}
