package com.simprints.id.data.db.events_sync.down.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.id.data.db.event.domain.models.EventType.*
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncScope.SubjectModuleScope
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncScope.SubjectUserScope
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncScope.SubjectProjectScope
import com.simprints.id.domain.modality.Modes

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = SubjectProjectScope::class),
    JsonSubTypes.Type(value = SubjectUserScope::class),
    JsonSubTypes.Type(value = SubjectModuleScope::class)
)
abstract class EventDownSyncScope(open val operations: List<EventDownSyncOperation> = mutableListOf()) {

    abstract val id: String

    data class SubjectProjectScope(val projectId: String,
                                   val modes: List<Modes>) : EventDownSyncScope() {
        override val id: String
            get() = "$projectId$separator" +
                modes.joinToString(separator)

        override val operations =
            listOf(EventDownSyncOperation(id, RemoteEventQuery(projectId, modes = modes, types = subjectEvents)))
    }

    data class SubjectUserScope(val projectId: String,
                                val attendantId: String,
                                val modes: List<Modes>) : EventDownSyncScope() {
        override val id: String
            get() =
                "$projectId$separator" +
                    "$attendantId$separator" +
                    modes.joinToString(separator)

        override val operations =
            listOf(EventDownSyncOperation(id, RemoteEventQuery(projectId, attendantId = attendantId, modes = modes, types = subjectEvents)))
    }

    data class SubjectModuleScope(val projectId: String,
                                  val moduleIds: List<String>,
                                  val modes: List<Modes>) : EventDownSyncScope() {
        override val id: String
            get() =
                "$projectId$separator" +
                    "${moduleIds.joinToString(separator)}$separator" +
                    modes.joinToString(separator)

        override val operations =
            listOf(EventDownSyncOperation(id, RemoteEventQuery(projectId, moduleIds = moduleIds, modes = modes, types = subjectEvents)))

    }

    companion object {
        const val PROJECT_SUBJECT_SYNC_KEY = "PROJECT_SUBJECT_SYNC"
        const val USER_SUBJECT_SYNC_KEY = "USER_SUBJECT_SYNC"
        const val MODULE_SUBJECT_SYNC_KEY = "MODULE_SUBJECT_SYNC"
        private val subjectEvents = listOf(ENROLMENT_RECORD_CREATION, ENROLMENT_RECORD_MOVE, ENROLMENT_RECORD_DELETION)
        private const val separator = "||"
    }
}
