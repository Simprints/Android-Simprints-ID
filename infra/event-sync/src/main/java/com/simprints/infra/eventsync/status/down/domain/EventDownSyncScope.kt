package com.simprints.infra.eventsync.status.down.domain

import androidx.annotation.Keep
import com.simprints.core.domain.common.Modality
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal sealed class EventDownSyncScope {
    abstract var operations: List<EventDownSyncOperation>

    @Keep
    @Serializable
    data class SubjectProjectScope(
        val projectId: String,
        val modes: List<Modality>,
    ) : EventDownSyncScope() {
        override var operations =
            listOf(
                EventDownSyncOperation(
                    RemoteEventQuery(
                        projectId,
                        modes = modes,
                    ),
                ),
            )
    }

    @Keep
    @Serializable
    data class SubjectUserScope(
        val projectId: String,
        val attendantId: String,
        val modes: List<Modality>,
    ) : EventDownSyncScope() {
        override var operations =
            listOf(
                EventDownSyncOperation(
                    RemoteEventQuery(
                        projectId,
                        attendantId = attendantId,
                        modes = modes,
                    ),
                ),
            )
    }

    @Keep
    @Serializable
    data class SubjectModuleScope(
        val projectId: String,
        val moduleIds: List<String>,
        val modes: List<Modality>,
    ) : EventDownSyncScope() {
        override var operations =
            moduleIds.map {
                EventDownSyncOperation(
                    RemoteEventQuery(
                        projectId,
                        moduleId = it,
                        modes = modes,
                    ),
                )
            }
    }
}
