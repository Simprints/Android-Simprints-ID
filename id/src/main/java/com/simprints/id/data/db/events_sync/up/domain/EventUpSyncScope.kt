package com.simprints.id.data.db.events_sync.up.domain

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncScope.Companion.PROJECT_SUBJECT_SYNC_KEY
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncScope.EventUpSyncScopeType.PROJECT_SUBJECT_SYNC
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncScope.SubjectProjectScope

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = SubjectProjectScope::class, name = PROJECT_SUBJECT_SYNC_KEY)
)
abstract class EventUpSyncScope(val type: EventUpSyncScopeType,
                                var operation: EventUpSyncOperation) {

    data class SubjectProjectScope(val projectId: String) :
        EventUpSyncScope(PROJECT_SUBJECT_SYNC, EventUpSyncOperation(LocalEventQuery(projectId = projectId))) {
    }

    enum class EventUpSyncScopeType {
        @JsonProperty(PROJECT_SUBJECT_SYNC_KEY) PROJECT_SUBJECT_SYNC
    }

    companion object {
        const val PROJECT_SUBJECT_SYNC_KEY = "PROJECT_SUBJECT_SYNC"

        private const val separator = "||"
    }

}
