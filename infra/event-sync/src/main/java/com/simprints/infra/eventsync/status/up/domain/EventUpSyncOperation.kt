package com.simprints.infra.eventsync.status.up.domain

import androidx.annotation.Keep
import java.util.UUID

@Keep
internal data class EventUpSyncOperation(
    val projectId: String,
    var lastState: UpSyncState? = null,
    var lastSyncTime: Long? = null,
) {
    @Keep
    enum class UpSyncState {
        RUNNING,
        COMPLETE,
        FAILED,
    }

    fun getUniqueKey() = UUID.nameUUIDFromBytes(projectId.toByteArray()).toString()
}
