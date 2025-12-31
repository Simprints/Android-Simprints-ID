package com.simprints.infra.eventsync.status.up.domain

import androidx.annotation.Keep
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
internal data class EventUpSyncOperation(
    val projectId: String,
    var lastState: UpSyncState? = null,
    var lastSyncTime: Long? = null,
) {
    @Keep
    @Serializable
    enum class UpSyncState {
        RUNNING,
        COMPLETE,
        FAILED,
    }

    fun getUniqueKey() = UUID.nameUUIDFromBytes(projectId.toByteArray()).toString()
}
