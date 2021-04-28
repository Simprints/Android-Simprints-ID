package com.simprints.id.data.db.events_sync.up.domain

import androidx.annotation.Keep
import java.util.*

@Keep
data class EventUpSyncOperation(val projectId: String,
                                var lastState: UpSyncState? = null,
                                var lastSyncTime: Long? = null) {
    @Keep
    enum class UpSyncState {
        RUNNING,
        COMPLETE,
        FAILED
    }
}

fun EventUpSyncOperation.getUniqueKey() = UUID.nameUUIDFromBytes(projectId.toByteArray()).toString()

