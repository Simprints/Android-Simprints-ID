package com.simprints.infra.eventsync.status.up.domain

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal sealed class EventUpSyncScope {
    abstract val operation: EventUpSyncOperation

    @Keep
    @Serializable
    data class ProjectScope(
        val projectId: String,
        override var operation: EventUpSyncOperation = EventUpSyncOperation(projectId = projectId),
    ) : EventUpSyncScope()
}
