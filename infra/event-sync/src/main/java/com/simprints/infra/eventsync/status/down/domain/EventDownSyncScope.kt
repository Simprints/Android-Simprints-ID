package com.simprints.infra.eventsync.status.down.domain

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.core.domain.modality.Modes
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope.SubjectModuleScope
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope.SubjectUserScope
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncScope.ProjectScope

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true,
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ProjectScope::class),
    JsonSubTypes.Type(value = SubjectUserScope::class),
    JsonSubTypes.Type(value = SubjectModuleScope::class),
)
@Keep
internal sealed class EventDownSyncScope(
    open var operations: List<EventDownSyncOperation> = mutableListOf(),
) {
    @Keep
    data class SubjectProjectScope(
        val projectId: String,
        val modes: List<Modes>,
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
    data class SubjectUserScope(
        val projectId: String,
        val attendantId: String,
        val modes: List<Modes>,
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
    data class SubjectModuleScope(
        val projectId: String,
        val moduleIds: List<String>,
        val modes: List<Modes>,
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
