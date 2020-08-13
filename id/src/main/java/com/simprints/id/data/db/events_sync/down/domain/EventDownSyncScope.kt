package com.simprints.id.data.db.events_sync.down.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.id.data.db.event.domain.models.EventType.*
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncScope.*
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncScope.SubjectProjectScope
import com.simprints.id.domain.modality.Modes

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = SubjectProjectScope::class),
    JsonSubTypes.Type(value = UserScope::class),
    JsonSubTypes.Type(value = ModuleScope::class),
    JsonSubTypes.Type(value = SubjectScope::class)
)
sealed class EventDownSyncScope(open var operations: List<EventDownSyncOperation> = mutableListOf()) {

    data class ProjectScope(val projectId: String,
                            val modes: List<Modes>) : EventDownSyncScope() {

        override var operations =
            listOf(EventDownSyncOperation(RemoteEventQuery(projectId, modes = modes, types = subjectEvents)))
    }

    data class UserScope(val projectId: String,
                         val attendantId: String,
                         val modes: List<Modes>) : EventDownSyncScope() {

        override var operations =
            listOf(EventDownSyncOperation(RemoteEventQuery(projectId, attendantId = attendantId, modes = modes, types = subjectEvents)))
    }

    data class ModuleScope(val projectId: String,
                           val moduleIds: List<String>,
                           val modes: List<Modes>) : EventDownSyncScope() {

        //The backend is capable to receive multiple modules, but SID is still making a request (operation) for each module
        override var operations =
            moduleIds.map {
                EventDownSyncOperation(RemoteEventQuery(projectId, moduleIds = listOf(it), modes = modes, types = subjectEvents))
            }

    }

    //To fetch 1 guid only
    data class SubjectScope(val projectId: String,
                            val subjectId: String,
                            val modes: List<Modes>) : EventDownSyncScope() {

        override var operations =
            listOf(EventDownSyncOperation( RemoteEventQuery(projectId, subjectId = subjectId, modes = modes, types = subjectEvents)))

    }

    companion object {
        private val subjectEvents = listOf(ENROLMENT_RECORD_CREATION, ENROLMENT_RECORD_MOVE, ENROLMENT_RECORD_DELETION)
    }
}
