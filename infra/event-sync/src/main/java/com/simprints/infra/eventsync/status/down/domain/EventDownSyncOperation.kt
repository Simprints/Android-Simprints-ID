package com.simprints.infra.eventsync.status.down.domain

import androidx.annotation.Keep
import java.util.UUID

@Keep
internal data class EventDownSyncOperation(
    val queryEvent: RemoteEventQuery,
    val state: DownSyncState? = null,
    val lastEventId: String? = null,
    val lastSyncTime: Long? = null,
) {
    @Keep
    enum class DownSyncState {
        RUNNING,
        COMPLETE,
        FAILED,
    }

    // Unique key: all request params expect for lastEventId
    internal fun getUniqueKey(): String = with(this.queryEvent) {
        UUID
            .nameUUIDFromBytes(
                (
                    projectId +
                        (attendantId ?: "") +
                        (subjectId ?: "") +
                        (moduleId ?: "") +
                        modes.joinToString { it.name } +
                        oldTypes
                ).toByteArray(),
            ).toString()
    }

    companion object {
        // We need to keep this old types otherwise the unique key of the down-sync will change
        // and we will need to down-sync again from scratch.
        internal var oldTypes = "ENROLMENT_RECORD_CREATION, ENROLMENT_RECORD_MOVE, ENROLMENT_RECORD_DELETION"
    }
}
