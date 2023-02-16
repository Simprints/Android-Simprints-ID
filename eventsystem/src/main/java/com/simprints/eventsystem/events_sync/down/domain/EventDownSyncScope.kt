package com.simprints.eventsystem.events_sync.down.domain

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.core.domain.modality.Modes
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope.SubjectModuleScope
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope.SubjectUserScope
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncScope.ProjectScope

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ProjectScope::class),
    JsonSubTypes.Type(value = SubjectUserScope::class),
    JsonSubTypes.Type(value = SubjectModuleScope::class)
)
@Keep
sealed class EventDownSyncScope(open var operations: List<EventDownSyncOperation> = mutableListOf()) {

    @Keep
    data class SubjectProjectScope(
        val projectId: String,
        val modes: List<Modes>
    ) : EventDownSyncScope() {

        override var operations =
            listOf(
                EventDownSyncOperation(
                    RemoteEventQuery(
                        projectId,
                        modes = modes,
                    )
                )
            )
    }

    @Keep
    data class SubjectUserScope(
        val projectId: String,
        val attendantId: String,
        val modes: List<Modes>
    ) : EventDownSyncScope() {

        override var operations =
            listOf(
                EventDownSyncOperation(
                    RemoteEventQuery(
                        projectId,
                        attendantId = attendantId,
                        modes = modes,
                    )
                )
            )
    }

    @Keep
    data class SubjectModuleScope(
        val projectId: String,
        val moduleIds: List<String>,
        val modes: List<Modes>
    ) : EventDownSyncScope() {

        //The backend is capable to receive multiple modules, but SID is still making a request (operation) for each module
        override var operations =
            moduleIds.map {
                EventDownSyncOperation(
                    RemoteEventQuery(
                        projectId,
                        moduleIds = listOf(it),
                        modes = modes,
                    )
                )
            }

    }
}
